package ru.massonnn.masutils.client.features.mining;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import ru.massonnn.masutils.Masutils;
import ru.massonnn.masutils.client.config.MasUtilsConfigManager;
import ru.massonnn.masutils.client.events.LocationEvents;
import ru.massonnn.masutils.client.events.WorldRenderExtractionCallback;
import ru.massonnn.masutils.client.hypixel.Location;
import ru.massonnn.masutils.client.utils.ModMessage;
import ru.massonnn.masutils.client.utils.render.primitive.PrimitiveCollector;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GrottoFinder {
    private static World lastWorld = null;
    public static final Set<Block> MAGENTA_GLASS_BLOCKS = new HashSet<>();
    public static final Set<Block> MAGENTA_GLASS_PANES = new HashSet<>();

    private static boolean isScanning = false;
    private static final Map<ChunkPos, StructureData> detectedStructures = new HashMap<>();
    private static final Set<ChunkPos> scannedChunks = new HashSet<>();
    private static World currentWorld = null;

    // Performance configuration
    private static final int MAX_BLOCKS_PER_CHUNK = 200; // Maximum magenta blocks to find before stopping
    private static final int SCAN_HEIGHT_ABOVE_SURFACE = 5; // How many blocks above surface to scan
    private static final int SCAN_HEIGHT_BELOW_SURFACE = 70; // How many blocks below surface to scan (underground focus)
    private static final int MIN_UNDERGROUND_Y = 10; // Minimum Y level to scan (avoid bedrock)

    public static final Map<String, StructureType> STRUCTURE_TYPES = new HashMap<>();

    static {
        // Add magenta glass blocks
        MAGENTA_GLASS_BLOCKS.add(Blocks.MAGENTA_STAINED_GLASS);

        // Add magenta glass panes
        MAGENTA_GLASS_PANES.add(Blocks.MAGENTA_STAINED_GLASS_PANE);


        // Define structure types
        STRUCTURE_TYPES.put("Shrine", new StructureType("Shrine", 86, 117));
        STRUCTURE_TYPES.put("Arch", new StructureType("Arch", 57, 70));
        STRUCTURE_TYPES.put("Mansion", new StructureType("Mansion", 340, 1));
        STRUCTURE_TYPES.put("Hall", new StructureType("Hall", 80, 19));
        STRUCTURE_TYPES.put("Pillars", new StructureType("Pillars", 101, 0));
        STRUCTURE_TYPES.put("Palace", new StructureType("Palace", 181, 104));
        STRUCTURE_TYPES.put("Remnants", new StructureType("Remnants", 90, 17));
        STRUCTURE_TYPES.put("Aqueduct", new StructureType("Aqueduct", 84, 0));
        STRUCTURE_TYPES.put("Other", new StructureType("Other", 0, 0)); // Special case for structures with >5 blocks/panes
    }

    public static class StructureType {
        public final String name;
        public final int expectedPanes;
        public final int expectedBlocks;

        public StructureType(String name, int expectedPanes, int expectedBlocks) {
            this.name = name;
            this.expectedPanes = expectedPanes;
            this.expectedBlocks = expectedBlocks;
        }
    }

    public static class StructureData {
        public final ChunkPos chunkPos;
        public final int paneCount;
        public final int blockCount;
        public final String structureType;
        public final BlockPos centerPos;
        public final java.util.List<BlockPos> foundBlocks;
        private final boolean notified;

        public StructureData(ChunkPos chunkPos, int paneCount, int blockCount, String structureType, BlockPos centerPos, java.util.List<BlockPos> foundBlocks) {
            this.chunkPos = chunkPos;
            this.paneCount = paneCount;
            this.blockCount = blockCount;
            this.structureType = structureType;
            this.centerPos = centerPos;
            this.foundBlocks = foundBlocks != null ? foundBlocks : new java.util.ArrayList<>();
            this.notified = false;
        }

        public StructureData(ChunkPos chunkPos, int paneCount, int blockCount, String structureType, BlockPos centerPos, java.util.List<BlockPos> foundBlocks, boolean notified) {
            this.chunkPos = chunkPos;
            this.paneCount = paneCount;
            this.blockCount = blockCount;
            this.structureType = structureType;
            this.centerPos = centerPos;
            this.foundBlocks = foundBlocks != null ? foundBlocks : new java.util.ArrayList<>();
            this.notified = notified;
        }

        public StructureData withNotified(boolean notified) {
            return new StructureData(chunkPos, paneCount, blockCount, structureType, centerPos, foundBlocks, notified);
        }

        public boolean isNotified() {
            return notified;
        }
    }

    public static void startScanning() {
        isScanning = true;
        lastScanX = Integer.MIN_VALUE;
        lastScanZ = Integer.MIN_VALUE;
        ModMessage.sendModMessage(Text.translatable("masutils.config.fiesta.grottoFinder.start"));
        scanAllLoadedChunks();
    }

    private static int lastScanX = Integer.MIN_VALUE;
    private static int lastScanZ = Integer.MIN_VALUE;
    private static final int MAX_CHUNKS_PER_TICK = 5;

    public static void initialize() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!MasUtilsConfigManager.get().fiestaConfig.grottoFinder) return;
            World currentWorld = client.world;
            if (currentWorld != null && currentWorld != lastWorld) {
                GrottoFinder.onWorldChanged(currentWorld);
                notifiedStructures.clear();
                lastWorld = currentWorld;
            }

            if (GrottoFinder.isScanning() && currentWorld != null) {
                GrottoFinder.scanAllLoadedChunks();
            }
        });

        ClientChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            if (!MasUtilsConfigManager.get().fiestaConfig.grottoFinder) return;
            if (GrottoFinder.isScanning() && world != null && chunk != null) {
                scanChunkForStructures(world, chunk);
            }
        });

        LocationEvents.ON_LOCATION_CHANGE.register((newLocation, prevLocation) -> {
            if (!MasUtilsConfigManager.get().fiestaConfig.grottoFinder) return;
            if (newLocation == Location.CRYSTAL_HOLLOWS && prevLocation != Location.CRYSTAL_HOLLOWS) {
                startScanning();
            } else {
                stopScanning();
                detectedStructures.clear();
                notifiedStructures.clear();
            }
        });

        WorldRenderExtractionCallback.EVENT.register(GrottoFinder::extractRendering);
    }
    private static void scanChunkForStructures(World world, Chunk chunk) {
        GrottoFinder.scanChunk(chunk, world);

        recheckIncompleteStructures(world, chunk.getPos());

        checkAndNotifyStructures(world);
    }

    private static void recheckIncompleteStructures(World world, net.minecraft.util.math.ChunkPos loadedChunkPos) {
        java.util.Set<net.minecraft.util.math.ChunkPos> processedStructureKeys = new java.util.HashSet<>();

        for (java.util.Map.Entry<net.minecraft.util.math.ChunkPos, GrottoFinder.StructureData> entry : GrottoFinder.getDetectedStructures().entrySet()) {
            GrottoFinder.StructureData structureData = entry.getValue();

            if (structureData == null || !"Incomplete".equals(structureData.structureType)) {
                continue;
            }

            if (processedStructureKeys.contains(structureData.chunkPos)) {
                continue;
            }

            processedStructureKeys.add(structureData.chunkPos);

            java.util.Set<net.minecraft.util.math.ChunkPos> structureChunks = new java.util.HashSet<>();
            if (structureData.foundBlocks != null && !structureData.foundBlocks.isEmpty()) {
                for (net.minecraft.util.math.BlockPos blockPos : structureData.foundBlocks) {
                    structureChunks.add(new net.minecraft.util.math.ChunkPos(blockPos));
                }
            }

            boolean chunkBelongsToStructure = structureChunks.contains(loadedChunkPos);
            if (!chunkBelongsToStructure) {
                continue;
            }

            boolean allChunksLoaded = true;
            for (net.minecraft.util.math.ChunkPos structChunkPos : structureChunks) {
                if (!world.isChunkLoaded(structChunkPos.x, structChunkPos.z)) {
                    allChunksLoaded = false;
                    break;
                }
            }

            if (allChunksLoaded) {
                for (net.minecraft.util.math.ChunkPos structChunkPos : structureChunks) {
                    if (world.isChunkLoaded(structChunkPos.x, structChunkPos.z)) {
                        try {
                            net.minecraft.world.chunk.Chunk chunk = world.getChunk(structChunkPos.x, structChunkPos.z);
                            GrottoFinder.scanChunk(chunk, world);
                        } catch (Exception e) {
                            Masutils.LOGGER.info("Exception on chunk rescanning: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private static final java.util.Set<net.minecraft.util.math.ChunkPos> notifiedStructures = new java.util.HashSet<>();

    private static void checkAndNotifyStructures(World world) {
        java.util.Set<net.minecraft.util.math.ChunkPos> processedStructureKeys = new java.util.HashSet<>();

        for (java.util.Map.Entry<net.minecraft.util.math.ChunkPos, GrottoFinder.StructureData> entry : GrottoFinder.getDetectedStructures().entrySet()) {
            GrottoFinder.StructureData structureData = entry.getValue();

            if (structureData == null || structureData.isNotified()) {
                continue;
            }

            if (processedStructureKeys.contains(structureData.chunkPos)) {
                continue;
            }

            processedStructureKeys.add(structureData.chunkPos);

            boolean allChunksScanned = true;
            java.util.Set<net.minecraft.util.math.ChunkPos> structureChunks = new java.util.HashSet<>();

            if (structureData.foundBlocks != null && !structureData.foundBlocks.isEmpty()) {
                for (net.minecraft.util.math.BlockPos blockPos : structureData.foundBlocks) {
                    structureChunks.add(new net.minecraft.util.math.ChunkPos(blockPos));
                }

                for (net.minecraft.util.math.ChunkPos structChunkPos : structureChunks) {
                    if (!world.isChunkLoaded(structChunkPos.x, structChunkPos.z)) {
                        allChunksScanned = false;
                        break;
                    }
                }
            }

            if (allChunksScanned && !structureChunks.isEmpty() && !notifiedStructures.contains(structureData.chunkPos) && !"Incomplete".equals(structureData.structureType)) {
                GrottoFinder.markStructureAsNotified(structureData.chunkPos);
                notifiedStructures.add(structureData.chunkPos);

                MinecraftClient client = MinecraftClient.getInstance();
                if (client != null && client.player != null) {
                    Text message = Text.translatable("masutils.mining.grottoFinder.found",
                            structureData.structureType,
                            structureData.chunkPos.x, structureData.chunkPos.z,
                            structureData.paneCount, structureData.blockCount,
                            structureChunks.size());
                    ModMessage.sendModMessage(message);
                }
            }
        }
    }
    public static void scanAllLoadedChunks() {
        net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
        if (client == null || client.world == null || !isScanning) {
            return;
        }

        World world = client.world;
        int chunksScannedThisTick = 0;

        if (client.player != null) {
            net.minecraft.util.math.ChunkPos playerChunk = new net.minecraft.util.math.ChunkPos(client.player.getBlockPos());
            int viewDistance = client.options.getViewDistance().getValue();

            int loadedChunksCount = 0;
            int unscannedLoadedChunks = 0;

            for (int x = -viewDistance; x <= viewDistance; x++) {
                for (int z = -viewDistance; z <= viewDistance; z++) {
                    int chunkX = playerChunk.x + x;
                    int chunkZ = playerChunk.z + z;
                    ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);

                    if (world.isChunkLoaded(chunkX, chunkZ)) {
                        loadedChunksCount++;
                        if (!scannedChunks.contains(chunkPos)) {
                            unscannedLoadedChunks++;
                        }
                    }
                }
            }

            if (unscannedLoadedChunks == 0 && loadedChunksCount > 0) {
                stopScanning();
                lastScanX = Integer.MIN_VALUE;
                lastScanZ = Integer.MIN_VALUE;
                return;
            }

            int startX = lastScanX == Integer.MIN_VALUE ? -viewDistance : lastScanX;
            int startZ = lastScanZ == Integer.MIN_VALUE ? -viewDistance : lastScanZ + 1;

            if (startZ > viewDistance) {
                startZ = -viewDistance;
                startX++;
            }

            if (startX > viewDistance) {
                startX = -viewDistance;
                startZ = -viewDistance;
            }

            for (int x = startX; x <= viewDistance && chunksScannedThisTick < MAX_CHUNKS_PER_TICK; x++) {
                int zStart = (x == startX) ? startZ : -viewDistance;
                for (int z = zStart; z <= viewDistance && chunksScannedThisTick < MAX_CHUNKS_PER_TICK; z++) {
                    int chunkX = playerChunk.x + x;
                    int chunkZ = playerChunk.z + z;
                    ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);

                    if (scannedChunks.contains(chunkPos)) {
                        continue;
                    }

                    if (world.isChunkLoaded(chunkX, chunkZ)) {
                        try {
                            Chunk chunk = world.getChunk(chunkX, chunkZ);
                            if (chunk != null) {
                                scanChunk(chunk, world);
                                chunksScannedThisTick++;
                                lastScanX = x;
                                lastScanZ = z;
                            }
                        } catch (Exception e) {
                            Masutils.LOGGER.info("Exception in scanning all chunks: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    public static void stopScanning() {
        if (isScanning) ModMessage.sendModMessage(Text.translatable("masutils.config.fiesta.grottoFinder.stop"));
        isScanning = false;
    }

    public static boolean isScanning() {
        return isScanning;
    }

    public static Map<ChunkPos, StructureData> getDetectedStructures() {
//        for (Map.Entry<ChunkPos, StructureData> entry : detectedStructures.entrySet()) {
//            StructureData data = entry.getValue();
//            if (data != null) {
//                System.out.println("[GrottoFinder] Structure at " + entry.getKey() + ": type=" + data.structureType + ", blocks=" + (data.foundBlocks != null ? data.foundBlocks.size() : 0));
//            }
//        }
        return detectedStructures;
    }

    public static void clearDetectedStructures() {
        detectedStructures.clear();
        scannedChunks.clear();
    }

    public static void onWorldChanged(World newWorld) {
        if (currentWorld != newWorld) {
            currentWorld = newWorld;
            if (newWorld == null) {
                detectedStructures.clear();
                scannedChunks.clear();
                notifiedStructures.clear();
            }
        }
    }

    public static boolean isMagentaGlass(Block block) {
        if (block == null) return false;
        return block == Blocks.MAGENTA_STAINED_GLASS || block == Blocks.MAGENTA_STAINED_GLASS_PANE;
    }

    public static boolean isMagentaGlassBlock(Block block) {
        return block == Blocks.MAGENTA_STAINED_GLASS;
    }

    public static boolean isMagentaGlassPane(Block block) {
        return block == Blocks.MAGENTA_STAINED_GLASS_PANE;
    }

    // Configuration getters
    public static int getMaxBlocksPerChunk() {
        return MAX_BLOCKS_PER_CHUNK;
    }

    public static int getScanHeightAboveSurface() {
        return SCAN_HEIGHT_ABOVE_SURFACE;
    }

    public static int getScanHeightBelowSurface() {
        return SCAN_HEIGHT_BELOW_SURFACE;
    }

    public static int getMinUndergroundY() {
        return MIN_UNDERGROUND_Y;
    }

    public static void scanChunk(Chunk chunk, World world) {
        if (!isScanning || world == null || chunk == null) {
            return;
        }

        ChunkPos chunkPos = chunk.getPos();

        StructureData existingStructure = detectedStructures.get(chunkPos);
        boolean isIncompleteStructure = existingStructure != null && "Incomplete".equals(existingStructure.structureType);

        if (scannedChunks.contains(chunkPos) && !isIncompleteStructure) {
            return;
        }

        if (isIncompleteStructure) {
            scannedChunks.remove(chunkPos);
        }


        ChunkScanResult currentResult = scanSingleChunk(chunk, world);

        scannedChunks.add(chunkPos);


        if (currentResult.paneCount > 0 || currentResult.blockCount > 0) {

            ChunkScanResult totalResult = scanNeighboringChunks(chunkPos, world, currentResult);

            java.util.Set<ChunkPos> structureChunks = new java.util.HashSet<>();
            for (BlockPos blockPos : totalResult.foundBlocks) {
                structureChunks.add(new ChunkPos(blockPos));
            }

            boolean allChunksLoaded = true;
            for (ChunkPos structChunkPos : structureChunks) {
                if (!world.isChunkLoaded(structChunkPos.x, structChunkPos.z)) {
                    allChunksLoaded = false;
                    break;
                }
            }

            if (!allChunksLoaded) {
                BlockPos centerPos = calculateStructureCenter(totalResult.foundBlocks);
                StructureData incompleteStructure = new StructureData(chunkPos, totalResult.paneCount, totalResult.blockCount, "Incomplete", centerPos, totalResult.foundBlocks, false);
                for (ChunkPos structChunkPos : structureChunks) {
                    detectedStructures.put(structChunkPos, incompleteStructure);
                }
                return;
            }

            String structureType = classifyStructure(totalResult.paneCount, totalResult.blockCount);

            if (structureType == null || "Other".equals(structureType)) {
                BlockPos centerPos = calculateStructureCenter(totalResult.foundBlocks);
                StructureData incompleteStructure = new StructureData(chunkPos, totalResult.paneCount, totalResult.blockCount, "Incomplete", centerPos, totalResult.foundBlocks, false);
                for (ChunkPos structChunkPos : structureChunks) {
                    detectedStructures.put(structChunkPos, incompleteStructure);
                }
                return;
            }


            BlockPos centerPos = calculateStructureCenter(totalResult.foundBlocks);

            StructureData existingCompleteStructure = null;
            for (ChunkPos structChunkPos : structureChunks) {
                StructureData existing = detectedStructures.get(structChunkPos);
                if (existing != null && !"Incomplete".equals(existing.structureType)) {
                    existingCompleteStructure = existing;
                    break;
                }
            }

            if (existingCompleteStructure != null) {
                if (totalResult.paneCount + totalResult.blockCount > existingCompleteStructure.paneCount + existingCompleteStructure.blockCount) {
                    StructureData updatedStructure = new StructureData(chunkPos, totalResult.paneCount, totalResult.blockCount, structureType, centerPos, totalResult.foundBlocks, existingCompleteStructure.isNotified());
                    for (ChunkPos structChunkPos : structureChunks) {
                        detectedStructures.put(structChunkPos, updatedStructure);
                    }
                }
            } else {
                StructureData structureData = new StructureData(chunkPos, totalResult.paneCount, totalResult.blockCount, structureType, centerPos, totalResult.foundBlocks, false);
                for (ChunkPos structChunkPos : structureChunks) {
                    detectedStructures.put(structChunkPos, structureData);
                }
            }
        }
    }

    public static void scanChunkManually(Chunk chunk, World world) {
        if (world == null || chunk == null) {
            return;
        }

        ChunkPos chunkPos = chunk.getPos();

        scannedChunks.remove(chunkPos);

        ChunkScanResult currentResult = scanSingleChunk(chunk, world);

        scannedChunks.add(chunkPos);


        if (currentResult.paneCount > 0 || currentResult.blockCount > 0) {

            ChunkScanResult totalResult = scanNeighboringChunks(chunkPos, world, currentResult);

            String structureType = classifyStructure(totalResult.paneCount, totalResult.blockCount);

            if (structureType != null) {
                BlockPos centerPos = new BlockPos(chunkPos.getStartX() + 8, 64, chunkPos.getStartZ() + 8);
                StructureData structureData = new StructureData(chunkPos, totalResult.paneCount, totalResult.blockCount, structureType, centerPos, totalResult.foundBlocks);
                detectedStructures.put(chunkPos, structureData);
            }
        }
    }

    private static class ChunkScanResult {
        public int paneCount = 0;
        public int blockCount = 0;
        public int scannedChunks = 0;
        public java.util.List<BlockPos> foundBlocks = new java.util.ArrayList<>();

        public ChunkScanResult() {}

        public ChunkScanResult(int paneCount, int blockCount, int scannedChunks) {
            this.paneCount = paneCount;
            this.blockCount = blockCount;
            this.scannedChunks = scannedChunks;
        }

        public void add(ChunkScanResult other) {
            this.paneCount += other.paneCount;
            this.blockCount += other.blockCount;
            this.scannedChunks += other.scannedChunks;
            this.foundBlocks.addAll(other.foundBlocks);
        }
    }

    private static ChunkScanResult scanSingleChunk(Chunk chunk, World world) {
        return scanSingleChunk(chunk, world, false);
    }

    private static ChunkScanResult scanSingleChunk(Chunk chunk, World world, boolean forceRescan) {
        net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
        if (client == null || client.world == null) {
            return new ChunkScanResult();
        }

        World clientWorld = client.world;
        ChunkPos chunkPos = chunk.getPos();
        ChunkScanResult result = new ChunkScanResult();
        int paneCount = 0;
        int blockCount = 0;
        int magentaBlocksFound = 0;

        int minY = Math.max(MIN_UNDERGROUND_Y, clientWorld.getBottomY());
        int chunkCenterX = chunkPos.getStartX() + 8;
        int chunkCenterZ = chunkPos.getStartZ() + 8;
        int surfaceY = clientWorld.getTopY(net.minecraft.world.Heightmap.Type.WORLD_SURFACE, chunkCenterX, chunkCenterZ);
        int maxY = Math.max(surfaceY + 64, 320);

        int chunkStartX = chunkPos.getStartX();
        int chunkStartZ = chunkPos.getStartZ();

        try {
            net.minecraft.world.chunk.ChunkSection[] sections = chunk.getSectionArray();

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    if (magentaBlocksFound > MAX_BLOCKS_PER_CHUNK) {
                        break;
                    }

                    for (int y = minY; y <= maxY; y++) {
                        if (magentaBlocksFound > MAX_BLOCKS_PER_CHUNK) {
                            break;
                        }

                        int sectionIndex = chunk.getSectionIndex(y);
                        if (sectionIndex >= 0 && sectionIndex < sections.length) {
                            net.minecraft.world.chunk.ChunkSection section = sections[sectionIndex];
                            if (section != null && !section.isEmpty()) {
                                BlockPos pos = new BlockPos(chunkStartX + x, y, chunkStartZ + z);
                                Block block = section.getBlockState(x, y & 15, z).getBlock();
                                if (isMagentaGlass(block)) {
                                    blockCount++;
                                    magentaBlocksFound++;
                                    result.foundBlocks.add(pos);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Masutils.LOGGER.info("Cannot check chunk due to exception: " + e.getMessage());
        }

        result.paneCount = paneCount;
        result.blockCount = blockCount;
        result.scannedChunks = 1;
        return result;
    }

    private static ChunkScanResult scanNeighboringChunks(ChunkPos centerChunk, World world, ChunkScanResult centerResult) {
        ChunkScanResult totalResult = new ChunkScanResult();
        totalResult.paneCount = centerResult.paneCount;
        totalResult.blockCount = centerResult.blockCount;
        totalResult.scannedChunks = centerResult.scannedChunks;
        totalResult.foundBlocks.addAll(centerResult.foundBlocks);
        java.util.Set<ChunkPos> structureChunks = new java.util.HashSet<>();
        structureChunks.add(centerChunk);

        scanNeighboringChunksRecursive(centerChunk, world, totalResult, structureChunks, 0);

        return totalResult;
    }

    private static void scanNeighboringChunksRecursive(ChunkPos currentChunk, World world, ChunkScanResult totalResult, java.util.Set<ChunkPos> structureChunks, int depth) {
        if (depth > 3) {
            return;
        }

        int[][] neighborOffsets = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

        for (int[] offset : neighborOffsets) {
            int neighborX = currentChunk.x + offset[0];
            int neighborZ = currentChunk.z + offset[1];
            ChunkPos neighborPos = new ChunkPos(neighborX, neighborZ);

            if (structureChunks.contains(neighborPos)) {
                continue;
            }

            if (world.isChunkLoaded(neighborX, neighborZ)) {
                try {
                    Chunk neighborChunk = world.getChunk(neighborX, neighborZ);

                    ChunkScanResult neighborResult = scanSingleChunk(neighborChunk, world, true);

                    if (!scannedChunks.contains(neighborPos)) {
                        scannedChunks.add(neighborPos);
                    }

                    if (neighborResult.paneCount > 0 || neighborResult.blockCount > 0) {
                        totalResult.add(neighborResult);
                        structureChunks.add(neighborPos);

                        scanNeighboringChunksRecursive(neighborPos, world, totalResult, structureChunks, depth + 1);
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    private static String classifyStructure(int paneCount, int blockCount) {
        int totalCount = paneCount + blockCount;

        if (totalCount > 0) {
            String bestMatch = null;
            double bestScore = Double.MAX_VALUE;

            for (Map.Entry<String, StructureType> entry : STRUCTURE_TYPES.entrySet()) {
                StructureType type = entry.getValue();

                if ("Other".equals(type.name)) continue;

                int paneDiff = Math.abs(paneCount - type.expectedPanes);
                int blockDiff = Math.abs(blockCount - type.expectedBlocks);

                boolean paneMatch;
                boolean blockMatch;

                if (type.expectedPanes == 0) {
                    paneMatch = paneCount <= 5;
                } else {
                    paneMatch = paneDiff <= Math.max(type.expectedPanes * 0.5, 10);
                }

                if (type.expectedBlocks == 0) {
                    blockMatch = blockCount <= 5;
                } else {
                    blockMatch = blockDiff <= Math.max(type.expectedBlocks * 0.5, 10);
                }

                if (paneMatch && blockMatch) {
                    double normalizedScore = (double)(paneDiff + blockDiff) / Math.max(type.expectedPanes + type.expectedBlocks, 1);
                    if (normalizedScore < bestScore) {
                        bestMatch = entry.getKey();
                        bestScore = normalizedScore;
                    }
                }
            }

            if (bestMatch == null) {
                for (Map.Entry<String, StructureType> entry : STRUCTURE_TYPES.entrySet()) {
                    StructureType type = entry.getValue();

                    if ("Other".equals(type.name)) continue;

                    int paneDiff = Math.abs(paneCount - type.expectedPanes);
                    int blockDiff = Math.abs(blockCount - type.expectedBlocks);
                    double normalizedScore = (double)(paneDiff + blockDiff) / Math.max(type.expectedPanes + type.expectedBlocks, 1);

                    if (normalizedScore < bestScore) {
                        bestMatch = entry.getKey();
                        bestScore = normalizedScore;
                    }
                }
            }

            return bestMatch != null ? bestMatch : "Other";
        }

        return null;
    }

    public static StructureData getStructureAt(ChunkPos chunkPos) {
        return detectedStructures.get(chunkPos);
    }

    public static StructureData findStructureContainingChunk(ChunkPos chunkPos) {
        for (StructureData structure : detectedStructures.values()) {
            if (structure.foundBlocks != null && !structure.foundBlocks.isEmpty()) {
                for (BlockPos blockPos : structure.foundBlocks) {
                    ChunkPos blockChunk = new ChunkPos(blockPos);
                    if (blockChunk.equals(chunkPos)) {
                        return structure;
                    }
                }
            }
        }
        return detectedStructures.get(chunkPos);
    }

    public static void markStructureAsNotified(ChunkPos chunkPos) {
        StructureData data = detectedStructures.get(chunkPos);
        if (data != null) {
            StructureData updatedData = data.withNotified(true);
            java.util.Set<ChunkPos> structureChunks = new java.util.HashSet<>();
            if (data.foundBlocks != null && !data.foundBlocks.isEmpty()) {
                for (BlockPos blockPos : data.foundBlocks) {
                    structureChunks.add(new ChunkPos(blockPos));
                }
            }
            structureChunks.add(chunkPos);

            for (ChunkPos structChunkPos : structureChunks) {
                detectedStructures.put(structChunkPos, updatedData);
            }
        }
    }

    private static BlockPos calculateStructureCenter(java.util.List<BlockPos> foundBlocks) {
        if (foundBlocks == null || foundBlocks.isEmpty()) {
            return new BlockPos(0, 64, 0);
        }

        long sumX = 0, sumY = 0, sumZ = 0;
        for (BlockPos pos : foundBlocks) {
            sumX += pos.getX();
            sumY += pos.getY();
            sumZ += pos.getZ();
        }

        int count = foundBlocks.size();
        return new BlockPos((int)(sumX / count), (int)(sumY / count), (int)(sumZ / count));
    }

    public static void extractRendering(PrimitiveCollector collector) {
        if (!MasUtilsConfigManager.get().fiestaConfig.grottoFinder) return;
        detectedStructures.forEach(((chunkPos, structureData) -> {
            for (BlockPos blockPos : structureData.foundBlocks) {
                collector.submitOutlinedBox(new Box(blockPos), Color.MAGENTA.getComponents(null), Color.MAGENTA.getAlpha(), 5f, true);
            }
        }));
    }
}

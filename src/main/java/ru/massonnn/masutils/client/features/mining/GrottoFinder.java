package ru.massonnn.masutils.client.features.mining;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import ru.massonnn.masutils.Masutils;
import ru.massonnn.masutils.client.config.MasUtilsConfigManager;
import ru.massonnn.masutils.client.events.LocationEvents;
import ru.massonnn.masutils.client.events.WorldRenderExtractionCallback;
import ru.massonnn.masutils.client.hypixel.Location;
import ru.massonnn.masutils.client.utils.ModMessage;
import ru.massonnn.masutils.client.utils.render.primitive.PrimitiveCollector;

import java.awt.*;
import java.awt.List;
import java.util.*;

public class GrottoFinder {
    private static World lastWorld = null;
    public static final Set<Block> MAGENTA_GLASS_BLOCKS = new HashSet<>();
    public static final Set<Block> MAGENTA_GLASS_PANES = new HashSet<>();

    private static boolean isScanning = false;
    private static World currentWorld = null;
    private static final Map<ChunkPos, StructureData> detectedStructures = new HashMap<>();
    private static final Map<ChunkPos, StructureData> rawChunkData = new HashMap<>();
    private static final Map<ChunkPos, StructureData> mergedStructures = new HashMap<>();
    private static final Set<ChunkPos> scannedChunks = new HashSet<>();

    // Performance configuration
    private static final int MAX_BLOCKS_PER_CHUNK = 1000; // Maximum magenta blocks to find before stopping
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
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        isScanning = true;
        lastScanX = Integer.MIN_VALUE;
        lastScanZ = Integer.MIN_VALUE;
        ModMessage.sendModMessage(Text.translatable("masutils.mining.grottoFinder.start"));
        scanAllLoadedChunks();

        if (client.options.getViewDistance().getValue() > 10) {
            ModMessage.sendModMessage(Text.translatable("masutils.mining.grottoFinder.renderHint"));
        }
    }

    private static int lastScanX = Integer.MIN_VALUE;
    private static int lastScanZ = Integer.MIN_VALUE;
    private static final int MAX_CHUNKS_PER_TICK = 5;

    private static int tickCounter;

    public static void initialize() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!MasUtilsConfigManager.get().fiestaConfig.grottoFinder) return;
            World world = client.world;
            if (world == null) return;

            if (world != lastWorld) {
                onWorldChanged(world);
                lastWorld = world;
            }

            if (isScanning) {
                scanAllLoadedChunks();

                if (++tickCounter >= 20) {
                    mergeAndClassify();
                    checkAndNotifyStructures(world);
                    tickCounter = 0;
                }
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

            boolean isNowInGrotto = (newLocation == Location.CRYSTAL_HOLLOWS);
            boolean wasInGrotto = (prevLocation == Location.CRYSTAL_HOLLOWS);

            if (isNowInGrotto && !wasInGrotto) {
                startScanning();
            }
            else if (!isNowInGrotto && wasInGrotto) {
                stopScanning();
                mergedStructures.clear();
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
        if (mergedStructures.isEmpty()) return;

        java.util.Set<StructureData> processedObjects = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());

        for (StructureData structureData : mergedStructures.values()) {
            if (structureData == null || structureData.isNotified() || !processedObjects.add(structureData)) {
                continue;
            }

            if ("Pending".equals(structureData.structureType) || "Incomplete".equals(structureData.structureType)) {
                continue;
            }

            java.util.Set<ChunkPos> structureChunks = new java.util.HashSet<>();
            for (BlockPos blockPos : structureData.foundBlocks) {
                structureChunks.add(new ChunkPos(blockPos));
            }

            boolean allChunksLoaded = true;
            for (ChunkPos cp : structureChunks) {
                if (!world.isChunkLoaded(cp.x, cp.z)) {
                    allChunksLoaded = false;
                    break;
                }
            }

            if (allChunksLoaded && !structureChunks.isEmpty() && !notifiedStructures.contains(structureData.chunkPos)) {

                markStructureAsNotified(structureData.chunkPos);
                notifiedStructures.add(structureData.chunkPos);

                MinecraftClient client = MinecraftClient.getInstance();
                if (client != null && client.player != null) {
                    if (structureData.chunkPos.x == 32 && structureData.chunkPos.z == 34) {
                        return;
                    }
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
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null || !isScanning) return;

        BlockPos playerPos = client.player.getBlockPos();
        int pX = playerPos.getX() >> 4;
        int pZ = playerPos.getZ() >> 4;
        int radius = client.options.getViewDistance().getValue();

        java.util.List<ChunkPos> spiral = new java.util.ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                spiral.add(new ChunkPos(pX + x, pZ + z));
            }
        }

        spiral.sort(java.util.Comparator.comparingDouble(pos ->
                Math.pow(pos.x - pX, 2) + Math.pow(pos.z - pZ, 2)));

        int chunksScannedThisTick = 0;
        var chunkManager = client.world.getChunkManager();

        for (ChunkPos pos : spiral) {
            if (chunksScannedThisTick >= MAX_CHUNKS_PER_TICK) break;
            if (scannedChunks.contains(pos)) continue;

            if (chunkManager.isChunkLoaded(pos.x, pos.z)) {
                Chunk chunk = chunkManager.getChunk(pos.x, pos.z, ChunkStatus.FULL, false);
                if (chunk != null) {
                    scanChunk(chunk, client.world);
                    chunksScannedThisTick++;
                }
            }
        }
    }

    public static void stopScanning() {
        if (isScanning) ModMessage.sendModMessage(Text.translatable("masutils.mining.grottoFinder.stop"));
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
        return mergedStructures;
    }

    public static void clearDetectedStructures() {
        mergedStructures.clear();
        scannedChunks.clear();
    }

    public static void onWorldChanged(World newWorld) {
        if (currentWorld != newWorld) {
            currentWorld = newWorld;
            rawChunkData.clear();
            mergedStructures.clear();
            scannedChunks.clear();
            notifiedStructures.clear();
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

    public static void scanChunk(Chunk chunk, World world) {
        if (!isScanning || world == null || chunk == null) return;
        ChunkPos chunkPos = chunk.getPos();
        if (scannedChunks.contains(chunkPos)) return;

        ChunkScanResult currentResult = scanSingleChunk(chunk, world, false);
        if (currentResult == null) return;

        scannedChunks.add(chunkPos);

        if (!currentResult.foundBlocks.isEmpty()) {
            StructureData rawData = new StructureData(
                    chunkPos,
                    currentResult.paneCount,
                    currentResult.blockCount,
                    "Pending",
                    calculateStructureCenter(currentResult.foundBlocks),
                    new java.util.ArrayList<>(currentResult.foundBlocks)
            );
            rawChunkData.put(chunkPos, rawData);
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
        if (chunk == null || world == null) return null;

        ChunkScanResult result = new ChunkScanResult();
        ChunkSection[] sections = chunk.getSectionArray();
        boolean chunkHasAnyData = false;
        int foundCount = 0;

        chunkScan:
        for (int sectionIdx = 0; sectionIdx < sections.length; sectionIdx++) {
            ChunkSection section = sections[sectionIdx];
            if (section == null || section.isEmpty()) continue;

            chunkHasAnyData = true;
            int baseY = chunk.sectionIndexToCoord(sectionIdx) << 4;

            for (int y = 0; y < 16; y++) {
                int realY = baseY + y;
                if (realY < MIN_UNDERGROUND_Y) continue;

                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        if (foundCount >= MAX_BLOCKS_PER_CHUNK) break chunkScan;

                        BlockState state = section.getBlockState(x, y, z);
                        Block block = state.getBlock();

                        if (isMagentaGlass(block)) {
                            BlockPos pos = new BlockPos(
                                    chunk.getPos().getStartX() + x,
                                    realY,
                                    chunk.getPos().getStartZ() + z
                            );

                            if (isMagentaGlassBlock(block)) result.blockCount++;
                            else result.paneCount++;

                            result.foundBlocks.add(pos);
                            foundCount++;
                        }
                    }
                }
            }
        }

        if (!chunkHasAnyData) return null;
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
        if (depth > 6) {
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
                    if (neighborResult == null) continue;
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
        return mergedStructures.get(chunkPos);
    }

    public static StructureData findStructureContainingChunk(ChunkPos chunkPos) {
        for (StructureData structure : mergedStructures.values()) {
            if (structure.foundBlocks != null && !structure.foundBlocks.isEmpty()) {
                for (BlockPos blockPos : structure.foundBlocks) {
                    ChunkPos blockChunk = new ChunkPos(blockPos);
                    if (blockChunk.equals(chunkPos)) {
                        return structure;
                    }
                }
            }
        }
        return mergedStructures.get(chunkPos);
    }

    public static void markStructureAsNotified(ChunkPos chunkPos) {
        StructureData data = mergedStructures.get(chunkPos);
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
                mergedStructures.put(structChunkPos, updatedData);
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

        // Рендерим сырые блоки из чанков (они появляются сразу при сканировании)
        // Здесь нет дубликатов, так как в rawChunkData один ChunkPos = один набор блоков
        for (StructureData data : rawChunkData.values()) {
            if (data == null || data.foundBlocks == null) continue;

            for (BlockPos blockPos : data.foundBlocks) {
                collector.submitOutlinedBox(
                        new Box(blockPos),
                        Color.MAGENTA.getComponents(null),
                        100f,
                        2.0f,
                        true
                );
            }
        }
    }

    private static void mergeAndClassify() {
        if (rawChunkData.isEmpty()) return;

        Set<ChunkPos> processed = new HashSet<>();
        Map<ChunkPos, StructureData> newMerged = new HashMap<>();

        for (ChunkPos startPos : rawChunkData.keySet()) {
            if (processed.contains(startPos)) continue;

            Set<ChunkPos> connectedChunks = new HashSet<>();
            java.util.List<BlockPos> allBlocks = new ArrayList<>();

            // Рекурсия теперь работает ТОЛЬКО по сырым данным
            collectRawRecursive(startPos, connectedChunks, allBlocks);

            if (connectedChunks.isEmpty()) continue;
            processed.addAll(connectedChunks);

            int totalPanes = 0, totalBlocks = 0;
            for (ChunkPos cp : connectedChunks) {
                StructureData chunkPart = rawChunkData.get(cp);
                if (chunkPart != null) {
                    totalPanes += chunkPart.paneCount;
                    totalBlocks += chunkPart.blockCount;
                }
            }

            String type = classifyStructure(totalPanes, totalBlocks);
            BlockPos center = calculateStructureCenter(allBlocks);

            StructureData finalStructure = new StructureData(
                    startPos, totalPanes, totalBlocks, type, center, allBlocks, false
            );

            for (ChunkPos cp : connectedChunks) {
                newMerged.put(cp, finalStructure);
            }
        }

        mergedStructures.clear();
        mergedStructures.putAll(newMerged);
    }

    private static void collectRawRecursive(ChunkPos current, Set<ChunkPos> visited, java.util.List<BlockPos> allBlocks) {
        if (visited.contains(current) || !rawChunkData.containsKey(current)) return;

        visited.add(current);
        StructureData data = rawChunkData.get(current);
        if (data != null) allBlocks.addAll(data.foundBlocks);

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                collectRawRecursive(new ChunkPos(current.x + x, current.z + z), visited, allBlocks);
            }
        }
    }

    private static void collectStructureRecursive(ChunkPos current, Set<ChunkPos> visited, java.util.List<BlockPos> allBlocks) {
        if (visited.contains(current) || !mergedStructures.containsKey(current)) return;

        visited.add(current);
        StructureData data = mergedStructures.get(current);
        if (data != null) allBlocks.addAll(data.foundBlocks);

        // Проверяем соседей (8 сторон)
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                collectStructureRecursive(new ChunkPos(current.x + x, current.z + z), visited, allBlocks);
            }
        }
    }

    private static void findConnectedChunks(ChunkPos current, Set<ChunkPos> visited, java.util.List<BlockPos> allBlocks) {
        if (visited.contains(current) || !mergedStructures.containsKey(current)) return;

        visited.add(current);
        StructureData data = mergedStructures.get(current);
        if (data != null && data.foundBlocks != null) {
            allBlocks.addAll(data.foundBlocks);
        }

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                findConnectedChunks(new ChunkPos(current.x + x, current.z + z), visited, allBlocks);
            }
        }
    }
}

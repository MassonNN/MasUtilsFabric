PROPERTIES_FILE := gradle.properties

UNAME_S := $(shell uname -s)
ifeq ($(UNAME_S),Darwin)
    SED_I := sed -i ''
else
    SED_I := sed -i
endif

MC_VER = $(shell grep "^minecraft_version=" $(PROPERTIES_FILE) | cut -d'=' -f2)

release:
	@$(SED_I) 's/^mod_version=.*/mod_version=$(version)/' $(PROPERTIES_FILE)
	
	$(eval FULL_TAG := v$(version)+$(MC_VER))

	git add $(PROPERTIES_FILE)
	git commit -m "Release $(FULL_TAG)"
	git tag -a $(FULL_TAG) -m "Version $(FULL_TAG)"
	
	git push origin $(FULL_TAG)

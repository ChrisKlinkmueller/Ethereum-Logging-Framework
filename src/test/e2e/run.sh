#!/bin/bash

function colorecho() {
	echo -e "\033[01;32m === $@ === \033[00m"
}

function redecho() {
	echo -e "\033[01;31m === $@ === \033[00m"
}

function main() {
	if [ -d 'extracted' ]; then
		if [ ! "$1" = "$AUTO_PARAM" ] && [ ! "$2" = "$AUTO_PARAM" ] && [ ! "$3" = "$AUTO_PARAM" ]; then
			echo "folder 'extracted' already exists! do you want to delete it and start again? (y/n)"
			read -r confirm
			if [ ! "$confirm" = "y" ]; then
				echo "aborting!"
				exit 1
			else
				colorecho "Cleaning up old test environment"
				rm -rf extracted
			fi
		else
			colorecho "Cleaning up old test environment"
			rm -rf extracted
		fi
	fi

	colorecho "Setting up test environment"
	mkdir -p extracted/{AugurContractRegistry,CryptoKitties,NetworkStatistics,Rebesky_Augur,Rebesky_ChickenHunt,Rebesky_Idex1}

	echo "$MAN_AugurContractRegistry" > extracted/AugurContractRegistry.bcql
	echo "$MAN_CryptoKitties" > extracted/CryptoKitties.bcql
	echo "$MAN_NetworkStatistics" > extracted/NetworkStatistics.bcql
	echo "$MAN_Rebesky_Augur" > extracted/Rebesky_Augur.bcql
	echo "$MAN_Rebesky_ChickenHunt" > extracted/Rebesky_ChickenHunt.bcql
	echo "$MAN_Rebesky_Idex1" > extracted/Rebesky_Idex1.bcql

	touch extracted/AugurContractRegistry/error.log.xelf
	touch extracted/CryptoKitties/error.log.xelf
	touch extracted/NetworkStatistics/error.log.xelf
	touch extracted/Rebesky_Augur/error.log.xelf
	touch extracted/Rebesky_ChickenHunt/error.log.xelf
	touch extracted/Rebesky_Idex1/error.log.xelf

	echo "$XELF_AugurContractRegistry" > extracted/AugurContractRegistry/all.log.xelf
	echo "$XELF_CryptoKitties" > extracted/CryptoKitties/log_pid0_all.xes.xelf
	echo "$XELF_NetworkStatistics" > extracted/NetworkStatistics/NetworkStatistics_all.csv.xelf
	echo "$XELF_Rebesky_Augur" > extracted/Rebesky_Augur/log_pid0_all.xes.xelf
	echo "$XELF_Rebesky_ChickenHunt" > extracted/Rebesky_ChickenHunt/log_pid0_all.xes.xelf
	echo "$XELF_Rebesky_Idex1" > extracted/Rebesky_Idex1/Idex_calls_all.csv.xelf

  if [ ! "$1" = "$SKIP_BUILD_PARAM" ] && [ ! "$2" = "$SKIP_BUILD_PARAM" ] && [ ! "$3" = "$SKIP_BUILD_PARAM" ]; then
	  colorecho "Building the BLF"
    WD="$SCRIPT_DIR/../../../"
    cd "$WD" || { redecho "Cannot cd into '$WD'. Probably the specified directory does not exist..." ; exit 2; }
    mvn package || { redecho "Bulding the BLF failed!"; exit 2; }
  fi

	# JAR=$(find $WD -iname "blf-*.jar" | grep -v "javadoc")
	JAR="$SCRIPT_DIR/../../../target/elf-cmd.jar"
	echo "$JAR"

	cd "$WD/extracted" || { redecho "Cannot cd into '$WD/extracted'. Probably the specified directory does not exist..." ; exit 2; }

	colorecho "Testing AugurContractRegistry"
  if [ "$1" = "$SILENT_PARAM" ] || [ "$2" = "$SILENT_PARAM" ] || [ "$3" = "$SILENT_PARAM" ]; then
	  java -jar "$JAR" extract AugurContractRegistry.bcql &> /dev/null
	else
	  java -jar "$JAR" extract AugurContractRegistry.bcql
  fi
	cmp AugurContractRegistry/all.log{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp AugurContractRegistry/error.log{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	colorecho "Test successful"

	colorecho "Testing CryptoKitties"
  if [ "$1" = "$SILENT_PARAM" ] || [ "$2" = "$SILENT_PARAM" ] || [ "$3" = "$SILENT_PARAM" ]; then
	  java -jar "$JAR" extract CryptoKitties.bcql &> /dev/null
	else
	  java -jar "$JAR" extract CryptoKitties.bcql
  fi
	cmp CryptoKitties/log_pid0_all.xes{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp CryptoKitties/error.log{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	colorecho "Test successful"

	colorecho "Testing NetworkStatistics"
  if [ "$1" = "$SILENT_PARAM" ] || [ "$2" = "$SILENT_PARAM" ] || [ "$3" = "$SILENT_PARAM" ]; then
	  java -jar "$JAR" extract NetworkStatistics.bcql &> /dev/null
	else
	  java -jar "$JAR" extract NetworkStatistics.bcql
  fi
	cmp NetworkStatistics/NetworkStatistics_all.csv{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp NetworkStatistics/error.log{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	colorecho "Test successful"

	colorecho "Testing Rebesky_Augur"
	if [ "$1" = "$SILENT_PARAM" ] || [ "$2" = "$SILENT_PARAM" ] || [ "$3" = "$SILENT_PARAM" ]; then
	  java -jar "$JAR" extract Rebesky_Augur.bcql &> /dev/null
	else
	  java -jar "$JAR" extract Rebesky_Augur.bcql
  fi
	cmp Rebesky_Augur/log_pid0_all.xes{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp Rebesky_Augur/error.log{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	colorecho "Test successful"

	colorecho "Testing Rebesky_ChickenHunt"
	if [ "$1" = "$SILENT_PARAM" ] || [ "$2" = "$SILENT_PARAM" ] || [ "$3" = "$SILENT_PARAM" ]; then
	  java -jar "$JAR" extract Rebesky_ChickenHunt.bcql &> /dev/null
	else
	  java -jar "$JAR" extract Rebesky_ChickenHunt.bcql
  fi
	cmp Rebesky_ChickenHunt/log_pid0_all.xes{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp Rebesky_ChickenHunt/error.log{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	colorecho "Test successful"

	colorecho "Testing Rebesky_Idex1"
	if [ "$1" = "$SILENT_PARAM" ] || [ "$2" = "$SILENT_PARAM" ] || [ "$3" = "$SILENT_PARAM" ]; then
	  java -jar "$JAR" extract Rebesky_Idex1.bcql &> /dev/null
	else
	  java -jar "$JAR" extract Rebesky_Idex1.bcql
  fi
	cmp Rebesky_Idex1/Idex_calls_all.csv{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp Rebesky_Idex1/error.log{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	colorecho "Test successful"

	colorecho "All tests completed successfully"
	cd "$WD" || { redecho "Cannot cd into '$WD'. Probably the specified directory does not exist..." ; exit 2; }
	if [ ! "$1" = "$AUTO_PARAM" ] && [ ! "$2" = "$AUTO_PARAM" ] && [ ! "$3" = "$AUTO_PARAM" ]; then
		echo "Remove the generated data? (y/n)"
		read -r confirm
		if [ "$confirm" = "y" ]; then
			echo "Deleting generated data"
			rm -rf extracted
		fi
		echo "Run mvn clean? (y/n)"
		read -r confirm
		if [ "$confirm" = "y" ]; then
			echo "mvn clean"
			mvn clean
		fi
	else
		echo "Deleting generated data"
		rm -rf extracted
	fi

	colorecho "Goodbye"
}

AUTO_PARAM="--auto"
SKIP_BUILD_PARAM="--auto"
SILENT_PARAM="--silent"

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

# BCQL scripts
MAN_AugurContractRegistry=$(cat "$SCRIPT_DIR/bcql/AugurContractRegistry.bcql")

MAN_CryptoKitties=$(cat "$SCRIPT_DIR/bcql/CryptoKitties.bcql")

MAN_NetworkStatistics=$(cat "$SCRIPT_DIR/bcql/NetworkStatistics.bcql")

MAN_Rebesky_Augur=$(cat "$SCRIPT_DIR/bcql/Rebesky_Augur.bcql")

MAN_Rebesky_ChickenHunt=$(cat "$SCRIPT_DIR/bcql/Rebesky_ChickenHunt.bcql")

MAN_Rebesky_Idex1=$(cat "$SCRIPT_DIR/bcql/Rebesky_Idex1.bcql")

# BCQL expected outputs
XELF_AugurContractRegistry=$(cat "$SCRIPT_DIR/outputs/AugurContractRegistry.o")

XELF_CryptoKitties=$(cat "$SCRIPT_DIR/outputs/CryptoKitties.o")

XELF_NetworkStatistics=$(cat "$SCRIPT_DIR/outputs/NetworkStatistics.o")

XELF_Rebesky_Augur=$(cat "$SCRIPT_DIR/outputs/Rebesky_Augur.o")

XELF_Rebesky_ChickenHunt=$(cat "$SCRIPT_DIR/outputs/Rebesky_ChickenHunt.o")

XELF_Rebesky_Idex1=$(cat "$SCRIPT_DIR/outputs/Rebesky_Idex1.o")

main "$@"

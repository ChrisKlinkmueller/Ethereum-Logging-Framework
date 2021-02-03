#!/bin/bash

function colorecho() {
	echo -e "\033[01;32m === $* === \033[00m"
}

function redecho() {
	echo -e "\033[01;31m === $* === \033[00m"
}

function main() {
	LWD=$PWD
	if [ -d "$ED" ]; then
		if [ ! "$1" = "$AUTO_PARAM" ] && [ ! "$2" = "$AUTO_PARAM" ] && [ ! "$3" = "$AUTO_PARAM" ]; then
			echo "folder 'extracted' already exists! do you want to delete it and start again? (y/n)"
			read -r confirm
			if [ ! "$confirm" = "y" ]; then
				echo "aborting!"
				exit 1
			else
				colorecho "Cleaning up old test environment"
				rm -rf "$ED"
			fi
		else
			colorecho "Cleaning up old test environment"
			rm -rf "$ED"
		fi
	fi

	colorecho "Setting up test environment"
	mkdir -p "$ED"/{AugurContractRegistry,CryptoKitties,NetworkStatistics,Rebesky_Augur,Rebesky_ChickenHunt,Rebesky_Idex1,HyperBasic,HyperKitties,CryptoKittiesAsHyper,FailingHyperKitties,FailingCryptoKitties,hyperledger}

  cp $CRED_DIR/* "$ED/hyperledger/"

  cp $BCQL_DIR/* "$ED/"

	touch "$ED"/AugurContractRegistry/error.log.xelf
	touch "$ED"/CryptoKitties/error.log.xelf
	touch "$ED"/NetworkStatistics/error.log.xelf
	touch "$ED"/Rebesky_Augur/error.log.xelf
	touch "$ED"/Rebesky_ChickenHunt/error.log.xelf
	touch "$ED"/Rebesky_Idex1/error.log.xelf
	touch "$ED"/HyperBasic/error.log.xblf
	touch "$ED"/HyperKitties/error.log.xblf
	touch "$ED"/CryptoKittiesAsHyper/error.log.xblf

  cp -r $XBLF_OUTPUTS_DIR/* "$ED/"

  if [ ! "$1" = "$SKIP_BUILD_PARAM" ] && [ ! "$2" = "$SKIP_BUILD_PARAM" ] && [ ! "$3" = "$SKIP_BUILD_PARAM" ]; then
	  colorecho "Building the BLF"
    cd "$WD" || { redecho "Cannot cd into '$WD'. Probably the specified directory does not exist..." ; exit 2; }
    mvn package || { redecho "Bulding the BLF failed!"; exit 2; }
	cd "$LWD"
  fi

	cd "$ED" || { redecho "Cannot cd into '$ED'. Probably the specified directory does not exist..." ; exit 2; }

  echo "JAR file should be located at '$JAR'"

	colorecho "Testing AugurContractRegistry"
	# Test with default batching mode
  if [ "$1" = "$SILENT_PARAM" ] || [ "$2" = "$SILENT_PARAM" ] || [ "$3" = "$SILENT_PARAM" ]; then
	  java -jar "$JAR" extract AugurContractRegistry.bcql &> /dev/null
	else
	  java -jar "$JAR" extract AugurContractRegistry.bcql
  fi
	cmp AugurContractRegistry/all.log{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp AugurContractRegistry/error.log{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	# Cleanup and test with safe batching mode
	rm -f AugurContractRegistry/{*.log,*.csv,*.xes}
	sed -i '/^SET OUTPUT FOLDER/a SET EMISSION MODE "safe batching"' AugurContractRegistry.bcql
  if [ "$1" = "$SILENT_PARAM" ] || [ "$2" = "$SILENT_PARAM" ] || [ "$3" = "$SILENT_PARAM" ]; then
	  java -jar "$JAR" extract AugurContractRegistry.bcql &> /dev/null
	else
	  java -jar "$JAR" extract AugurContractRegistry.bcql
  fi
	cmp AugurContractRegistry/all.log{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp AugurContractRegistry/error.log{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	colorecho "Test successful"

	colorecho "Testing CryptoKitties"
	# Test with default batching mode
  if [ "$1" = "$SILENT_PARAM" ] || [ "$2" = "$SILENT_PARAM" ] || [ "$3" = "$SILENT_PARAM" ]; then
	  java -jar "$JAR" extract CryptoKitties.bcql &> /dev/null
	else
	  java -jar "$JAR" extract CryptoKitties.bcql
  fi
	cmp CryptoKitties/log_pid0_all.xes{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp CryptoKitties/error.log{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	# Cleanup and test with safe batching mode
	rm -f CryptoKitties/{*.log,*.csv,*.xes}
	sed -i '/^SET OUTPUT FOLDER/a SET EMISSION MODE "safe batching"' CryptoKitties.bcql
  if [ "$1" = "$SILENT_PARAM" ] || [ "$2" = "$SILENT_PARAM" ] || [ "$3" = "$SILENT_PARAM" ]; then
	  java -jar "$JAR" extract CryptoKitties.bcql &> /dev/null
	else
	  java -jar "$JAR" extract CryptoKitties.bcql
  fi
	cmp CryptoKitties/log_pid0_all.xes{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp CryptoKitties/error.log{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	colorecho "Test successful"

	colorecho "Testing NetworkStatistics"
	# Test with default batching mode
  if [ "$1" = "$SILENT_PARAM" ] || [ "$2" = "$SILENT_PARAM" ] || [ "$3" = "$SILENT_PARAM" ]; then
	  java -jar "$JAR" extract NetworkStatistics.bcql &> /dev/null
	else
	  java -jar "$JAR" extract NetworkStatistics.bcql
  fi
	cmp NetworkStatistics/NetworkStatistics_all.csv{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp NetworkStatistics/error.log{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	# Cleanup and test with safe batching mode
	rm -f NetworkStatistics/{*.log,*.csv,*.xes}
	sed -i '/^SET OUTPUT FOLDER/a SET EMISSION MODE "safe batching"' NetworkStatistics.bcql
  if [ "$1" = "$SILENT_PARAM" ] || [ "$2" = "$SILENT_PARAM" ] || [ "$3" = "$SILENT_PARAM" ]; then
	  java -jar "$JAR" extract NetworkStatistics.bcql &> /dev/null
	else
	  java -jar "$JAR" extract NetworkStatistics.bcql
  fi
	cmp NetworkStatistics/NetworkStatistics_all.csv{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp NetworkStatistics/error.log{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	colorecho "Test successful"

	colorecho "Testing Rebesky_Augur"
	# Test with default batching mode
	if [ "$1" = "$SILENT_PARAM" ] || [ "$2" = "$SILENT_PARAM" ] || [ "$3" = "$SILENT_PARAM" ]; then
	  java -jar "$JAR" extract Rebesky_Augur.bcql &> /dev/null
	else
	  java -jar "$JAR" extract Rebesky_Augur.bcql
  fi
	cmp Rebesky_Augur/log_pid0_all.xes{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp Rebesky_Augur/error.log{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	# Cleanup and test with safe batching mode
	rm -f Rebesky_Augur/{*.log,*.csv,*.xes}
	sed -i '/^SET OUTPUT FOLDER/a SET EMISSION MODE "safe batching"' Rebesky_Augur.bcql
  if [ "$1" = "$SILENT_PARAM" ] || [ "$2" = "$SILENT_PARAM" ] || [ "$3" = "$SILENT_PARAM" ]; then
	  java -jar "$JAR" extract Rebesky_Augur.bcql &> /dev/null
	else
	  java -jar "$JAR" extract Rebesky_Augur.bcql
  fi
	cmp Rebesky_Augur/log_pid0_all.xes{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp Rebesky_Augur/error.log{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	colorecho "Test successful"

	colorecho "Testing Rebesky_ChickenHunt"
	# Test with default batching mode
	if [ "$1" = "$SILENT_PARAM" ] || [ "$2" = "$SILENT_PARAM" ] || [ "$3" = "$SILENT_PARAM" ]; then
	  java -jar "$JAR" extract Rebesky_ChickenHunt.bcql &> /dev/null
	else
	  java -jar "$JAR" extract Rebesky_ChickenHunt.bcql
  fi
	cmp Rebesky_ChickenHunt/log_pid0_all.xes{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp Rebesky_ChickenHunt/error.log{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	# Cleanup and test with safe batching mode
	rm -f Rebesky_ChickenHunt/{*.log,*.csv,*.xes}
	sed -i '/^SET OUTPUT FOLDER/a SET EMISSION MODE "safe batching"' Rebesky_ChickenHunt.bcql
  if [ "$1" = "$SILENT_PARAM" ] || [ "$2" = "$SILENT_PARAM" ] || [ "$3" = "$SILENT_PARAM" ]; then
	  java -jar "$JAR" extract Rebesky_ChickenHunt.bcql &> /dev/null
	else
	  java -jar "$JAR" extract Rebesky_ChickenHunt.bcql
  fi
	cmp Rebesky_ChickenHunt/log_pid0_all.xes{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp Rebesky_ChickenHunt/error.log{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	colorecho "Test successful"

	colorecho "Testing Rebesky_Idex1"
	# Test with default batching mode
	if [ "$1" = "$SILENT_PARAM" ] || [ "$2" = "$SILENT_PARAM" ] || [ "$3" = "$SILENT_PARAM" ]; then
	  java -jar "$JAR" extract Rebesky_Idex1.bcql &> /dev/null
	else
	  java -jar "$JAR" extract Rebesky_Idex1.bcql
  fi
	cmp Rebesky_Idex1/Idex_calls_all.csv{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp Rebesky_Idex1/error.log{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	# Cleanup and test with safe batching mode
	rm -f Rebesky_Idex1/{*.log,*.csv,*.xes}
	sed -i '/^SET OUTPUT FOLDER/a SET EMISSION MODE "safe batching"' Rebesky_Idex1.bcql
  if [ "$1" = "$SILENT_PARAM" ] || [ "$2" = "$SILENT_PARAM" ] || [ "$3" = "$SILENT_PARAM" ]; then
	  java -jar "$JAR" extract Rebesky_Idex1.bcql &> /dev/null
	else
	  java -jar "$JAR" extract Rebesky_Idex1.bcql
  fi
	cmp Rebesky_Idex1/Idex_calls_all.csv{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp Rebesky_Idex1/error.log{,.xelf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	colorecho "Test successful"

	colorecho "Testing HyperBasic"
	# Test with default batching mode
	if [ "$1" = "$SILENT_PARAM" ] || [ "$2" = "$SILENT_PARAM" ] || [ "$3" = "$SILENT_PARAM" ]; then
	  java -jar "$JAR" extract HyperBasic.bcql &> /dev/null
	else
	  java -jar "$JAR" extract HyperBasic.bcql
  fi
	cmp HyperBasic/all.log{,.xblf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp HyperBasic/log_testEvent_all.xes{,.xblf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp HyperBasic/payload_all.csv{,.xblf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp HyperBasic/error.log{,.xblf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	# Cleanup and test with safe batching mode
	rm -f HyperBasic/{*.log,*.csv,*.xes}
	sed -i '/^SET OUTPUT FOLDER/a SET EMISSION MODE "safe batching"' HyperBasic.bcql
  if [ "$1" = "$SILENT_PARAM" ] || [ "$2" = "$SILENT_PARAM" ] || [ "$3" = "$SILENT_PARAM" ]; then
	  java -jar "$JAR" extract HyperBasic.bcql &> /dev/null
	else
	  java -jar "$JAR" extract HyperBasic.bcql
  fi
	cmp HyperBasic/all.log{,.xblf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp HyperBasic/log_testEvent_all.xes{,.xblf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp HyperBasic/payload_all.csv{,.xblf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp HyperBasic/error.log{,.xblf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	colorecho "Test successful"

	colorecho "Testing HyperKitties"
	# Test with default batching mode
	if [ "$1" = "$SILENT_PARAM" ] || [ "$2" = "$SILENT_PARAM" ] || [ "$3" = "$SILENT_PARAM" ]; then
	  java -jar "$JAR" extract HyperKitties.bcql &> /dev/null
	else
	  java -jar "$JAR" extract HyperKitties.bcql
  fi
	cmp HyperKitties/all.log{,.xblf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp HyperKitties/Birth_all.csv{,.xblf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp HyperKitties/log_Birth_all.xes{,.xblf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp HyperKitties/log_Pregnant_all.xes{,.xblf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp HyperKitties/log_Transfer_all.xes{,.xblf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp HyperKitties/Pregnant_all.csv{,.xblf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp HyperKitties/Transfer_all.csv{,.xblf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp HyperKitties/error.log{,.xblf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	# Cleanup and test with safe batching mode
	rm -f HyperKitties/{*.log,*.csv,*.xes}
	sed -i '/^SET OUTPUT FOLDER/a SET EMISSION MODE "safe batching"' HyperKitties.bcql
  if [ "$1" = "$SILENT_PARAM" ] || [ "$2" = "$SILENT_PARAM" ] || [ "$3" = "$SILENT_PARAM" ]; then
	  java -jar "$JAR" extract HyperKitties.bcql &> /dev/null
	else
	  java -jar "$JAR" extract HyperKitties.bcql
  fi
	cmp HyperKitties/all.log{,.xblf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp HyperKitties/Birth_all.csv{,.xblf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp HyperKitties/log_Birth_all.xes{,.xblf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp HyperKitties/log_Pregnant_all.xes{,.xblf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp HyperKitties/log_Transfer_all.xes{,.xblf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp HyperKitties/Pregnant_all.csv{,.xblf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp HyperKitties/Transfer_all.csv{,.xblf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp HyperKitties/error.log{,.xblf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	colorecho "Test successful"

	colorecho "Testing CryptoKittiesAsHyper"
	# Test with default batching mode
	if [ "$1" = "$SILENT_PARAM" ] || [ "$2" = "$SILENT_PARAM" ] || [ "$3" = "$SILENT_PARAM" ]; then
	  java -jar "$JAR" extract CryptoKittiesAsHyper.bcql &> /dev/null
	else
	  java -jar "$JAR" extract CryptoKittiesAsHyper.bcql
  fi
	cmp CryptoKittiesAsHyper/log_pid0_all.xes{,.xblf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp CryptoKittiesAsHyper/error.log{,.xblf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	# Cleanup and test with safe batching mode
	rm -f CryptoKittiesAsHyper/{*.log,*.csv,*.xes}
	sed -i '/^SET OUTPUT FOLDER/a SET EMISSION MODE "safe batching"' CryptoKittiesAsHyper.bcql
  if [ "$1" = "$SILENT_PARAM" ] || [ "$2" = "$SILENT_PARAM" ] || [ "$3" = "$SILENT_PARAM" ]; then
	  java -jar "$JAR" extract CryptoKittiesAsHyper.bcql &> /dev/null
	else
	  java -jar "$JAR" extract CryptoKittiesAsHyper.bcql
  fi
	cmp CryptoKittiesAsHyper/log_pid0_all.xes{,.xblf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	cmp CryptoKittiesAsHyper/error.log{,.xblf} || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	colorecho "Test successful"

	colorecho "Testing FailingHyperKitties"
	LAST_EXIT=0
	if [ "$1" = "$SILENT_PARAM" ] || [ "$2" = "$SILENT_PARAM" ] || [ "$3" = "$SILENT_PARAM" ]; then
	  java -jar "$JAR" extract FailingHyperKitties.bcql &> /dev/null
	  LAST_EXIT=$?
	else
	  java -jar "$JAR" extract FailingHyperKitties.bcql
	  LAST_EXIT=$?
  fi
  [ $LAST_EXIT -ne 1 ] && { redecho "The program did not return the correct error code" ; exit 2; }
	grep 'JSON object does not contain key: a' FailingHyperKitties/error.log || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	colorecho "Test successful"

	colorecho "Testing FailingCryptoKitties"
	LAST_EXIT=0
	if [ "$1" = "$SILENT_PARAM" ] || [ "$2" = "$SILENT_PARAM" ] || [ "$3" = "$SILENT_PARAM" ]; then
	  java -jar "$JAR" extract FailingCryptoKitties.bcql &> /dev/null
	  LAST_EXIT=$?
	else
	  java -jar "$JAR" extract FailingCryptoKitties.bcql
	  LAST_EXIT=$?
  fi
  [ $LAST_EXIT -ne 1 ] && { redecho "The program did not return the correct error code" ; exit 2; }
	grep 'Error when processing block number' FailingCryptoKitties/error.log || { redecho "Comparing the extracted data with the expected data failed! Leaving test environment as is for investigation" ; exit 2; }
	colorecho "Test successful"

	colorecho "All tests completed successfully"
	cd "$LWD"
	if [ ! "$1" = "$AUTO_PARAM" ] && [ ! "$2" = "$AUTO_PARAM" ] && [ ! "$3" = "$AUTO_PARAM" ]; then
		echo "Remove the generated data? (y/n)"
		read -r confirm
		if [ "$confirm" = "y" ]; then
			echo "Deleting generated data"
			rm -rf "$ED"
		fi
		echo "Run mvn clean? (y/n)"
		read -r confirm
		if [ "$confirm" = "y" ]; then
			cd "$WD" || { redecho "Cannot cd into '$WD'. Probably the specified directory does not exist..." ; exit 2; }
			echo "mvn clean"
			mvn clean
		fi
	else
		echo "Deleting generated data"
		rm -rf "$ED"
	fi

	colorecho "Goodbye"
}

AUTO_PARAM="--auto"
SKIP_BUILD_PARAM="--skip-build"
SILENT_PARAM="--silent"

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
WD="$SCRIPT_DIR"/../../..
ED="./extracted"
JAR="$WD"/target/blf-cmd.jar

# HyperLedger credentials
CRED_DIR="$SCRIPT_DIR/credentials"

# BCQL scripts
BCQL_DIR="$SCRIPT_DIR/bcql"

# BCQL expected outputs
XBLF_OUTPUTS_DIR="$SCRIPT_DIR/outputs"

main "$@"

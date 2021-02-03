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

	echo "$CRED_Connection" > "$ED"/hyperledger/connection-org1.yaml
	echo "$CRED_Server_CRT" > "$ED"/hyperledger/server.crt
	echo "$CRED_Server_KEY" > "$ED"/hyperledger/server.key

	echo "$MAN_AugurContractRegistry" > "$ED"/AugurContractRegistry.bcql
	echo "$MAN_CryptoKitties" > "$ED"/CryptoKitties.bcql
	echo "$MAN_NetworkStatistics" > "$ED"/NetworkStatistics.bcql
	echo "$MAN_Rebesky_Augur" > "$ED"/Rebesky_Augur.bcql
	echo "$MAN_Rebesky_ChickenHunt" > "$ED"/Rebesky_ChickenHunt.bcql
	echo "$MAN_Rebesky_Idex1" > "$ED"/Rebesky_Idex1.bcql
  echo "$MAN_HyperBasic" > "$ED"/HyperBasic.bcql
  echo "$MAN_HyperKitties" > "$ED"/HyperKitties.bcql
  echo "$MAN_CryptoKittiesAsHyper" > "$ED"/CryptoKittiesAsHyper.bcql
  echo "$MAN_FailingHyperKitties" > "$ED"/FailingHyperKitties.bcql
  echo "$MAN_FailingCryptoKitties" > "$ED"/FailingCryptoKitties.bcql

	touch "$ED"/AugurContractRegistry/error.log.xelf
	touch "$ED"/CryptoKitties/error.log.xelf
	touch "$ED"/NetworkStatistics/error.log.xelf
	touch "$ED"/Rebesky_Augur/error.log.xelf
	touch "$ED"/Rebesky_ChickenHunt/error.log.xelf
	touch "$ED"/Rebesky_Idex1/error.log.xelf
	touch "$ED"/HyperBasic/error.log.xblf
	touch "$ED"/HyperKitties/error.log.xblf
	touch "$ED"/CryptoKittiesAsHyper/error.log.xblf

	echo "$XELF_AugurContractRegistry" > "$ED"/AugurContractRegistry/all.log.xelf
	echo "$XELF_CryptoKitties" > "$ED"/CryptoKitties/log_pid0_all.xes.xelf
	echo "$XELF_NetworkStatistics" > "$ED"/NetworkStatistics/NetworkStatistics_all.csv.xelf
	echo "$XELF_Rebesky_Augur" > "$ED"/Rebesky_Augur/log_pid0_all.xes.xelf
	echo "$XELF_Rebesky_ChickenHunt" > "$ED"/Rebesky_ChickenHunt/log_pid0_all.xes.xelf
	echo "$XELF_Rebesky_Idex1" > "$ED"/Rebesky_Idex1/Idex_calls_all.csv.xelf
	echo "$XBLF_HyperBasic_all" > "$ED"/HyperBasic/all.log.xblf
	echo "$XBLF_HyperBasic_log_testEvent" > "$ED"/HyperBasic/log_testEvent_all.xes.xblf
	echo "$XBLF_HyperBasic_payload" > "$ED"/HyperBasic/payload_all.csv.xblf
	echo "$XBLF_HyperKitties_all" > "$ED"/HyperKitties/all.log.xblf
	echo "$XBLF_HyperKitties_Birth_all" > "$ED"/HyperKitties/Birth_all.csv.xblf
	echo "$XBLF_HyperKitties_log_Birth_all" > "$ED"/HyperKitties/log_Birth_all.xes.xblf
	echo "$XBLF_HyperKitties_log_Pregnant_all" > "$ED"/HyperKitties/log_Pregnant_all.xes.xblf
	echo "$XBLF_HyperKitties_log_Transfer_all" > "$ED"/HyperKitties/log_Transfer_all.xes.xblf
	echo "$XBLF_HyperKitties_Pregnant_all" > "$ED"/HyperKitties/Pregnant_all.csv.xblf
	echo "$XBLF_HyperKitties_Transfer_all" > "$ED"/HyperKitties/Transfer_all.csv.xblf
	echo "$XBLF_CryptoKittiesAsHyper_all" > "$ED"/CryptoKittiesAsHyper/log_pid0_all.xes.xblf

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
CRED_Connection=$(cat "$SCRIPT_DIR/credentials/connection-org1.yaml")

CRED_Server_CRT=$(cat "$SCRIPT_DIR/credentials/server.crt")

CRED_Server_KEY=$(cat "$SCRIPT_DIR/credentials/server.key")

# BCQL scripts
MAN_AugurContractRegistry=$(cat "$SCRIPT_DIR"/bcql/AugurContractRegistry.bcql)

MAN_CryptoKitties=$(cat "$SCRIPT_DIR"/bcql/CryptoKitties.bcql)

MAN_NetworkStatistics=$(cat "$SCRIPT_DIR"/bcql/NetworkStatistics.bcql)

MAN_Rebesky_Augur=$(cat "$SCRIPT_DIR"/bcql/Rebesky_Augur.bcql)

MAN_Rebesky_ChickenHunt=$(cat "$SCRIPT_DIR"/bcql/Rebesky_ChickenHunt.bcql)

MAN_Rebesky_Idex1=$(cat "$SCRIPT_DIR"/bcql/Rebesky_Idex1.bcql)

MAN_HyperBasic=$(cat "$SCRIPT_DIR/bcql/HyperBasic.bcql")

MAN_HyperKitties=$(cat "$SCRIPT_DIR/bcql/HyperKitties.bcql")

MAN_CryptoKittiesAsHyper=$(cat "$SCRIPT_DIR/bcql/CryptoKittiesAsHyper.bcql")

MAN_FailingHyperKitties=$(cat "$SCRIPT_DIR/bcql/FailingHyperKitties.bcql")

MAN_FailingCryptoKitties=$(cat "$SCRIPT_DIR/bcql/FailingCryptoKitties.bcql")

# BCQL expected outputs
XELF_AugurContractRegistry=$(cat "$SCRIPT_DIR"/outputs/AugurContractRegistry.o)

XELF_CryptoKitties=$(cat "$SCRIPT_DIR"/outputs/CryptoKitties.o)

XELF_NetworkStatistics=$(cat "$SCRIPT_DIR"/outputs/NetworkStatistics.o)

XELF_Rebesky_Augur=$(cat "$SCRIPT_DIR"/outputs/Rebesky_Augur.o)

XELF_Rebesky_ChickenHunt=$(cat "$SCRIPT_DIR"/outputs/Rebesky_ChickenHunt.o)

XELF_Rebesky_Idex1=$(cat "$SCRIPT_DIR"/outputs/Rebesky_Idex1.o)

XBLF_HyperBasic_all=$(cat "$SCRIPT_DIR/outputs/HyperBasic_all.o")

XBLF_HyperBasic_log_testEvent=$(cat "$SCRIPT_DIR/outputs/HyperBasic_log_testEvent_all.o")

XBLF_HyperBasic_payload=$(cat "$SCRIPT_DIR/outputs/HyperBasic_payload_all.o")

XBLF_HyperKitties_all=$(cat "$SCRIPT_DIR/outputs/HyperKitties_all.o")

XBLF_HyperKitties_Birth_all=$(cat "$SCRIPT_DIR/outputs/HyperKitties_Birth_all.o")

XBLF_HyperKitties_log_Birth_all=$(cat "$SCRIPT_DIR/outputs/HyperKitties_log_Birth_all.o")

XBLF_HyperKitties_log_Pregnant_all=$(cat "$SCRIPT_DIR/outputs/HyperKitties_log_Pregnant_all.o")

XBLF_HyperKitties_log_Transfer_all=$(cat "$SCRIPT_DIR/outputs/HyperKitties_log_Transfer_all.o")

XBLF_HyperKitties_Pregnant_all=$(cat "$SCRIPT_DIR/outputs/HyperKitties_Pregnant_all.o")

XBLF_HyperKitties_Transfer_all=$(cat "$SCRIPT_DIR/outputs/HyperKitties_Transfer_all.o")

XBLF_CryptoKittiesAsHyper_all=$(cat "$SCRIPT_DIR/outputs/CryptoKittiesAsHyper_log_pid0_all.o")

main "$@"

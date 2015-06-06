<?php

class AcraAdapter
{
	const APP_ID = "a";
	const APP_VERSION_NAME = "b";
	const APP_VERSION_CODE = "c";
	const ANDROID_VERSION = "d";
	const USER_APP_START_DATE = "e";
	const USER_CRASH_DATE = "f";
	const REPORT_ID = "r";
	const PHONE_MODEL = "g";
	const BRAND = "h";
	const STACK_TRACE = "v";
	const USER_COMMENT = "w";

	private static $keyMap = [
		self::APP_VERSION_NAME    => 'APP_VERSION_NAME',
		self::APP_VERSION_CODE    => 'APP_VERSION_CODE',
		self::ANDROID_VERSION     => 'ANDROID_VERSION',
		self::USER_APP_START_DATE => 'USER_APP_START_DATE',
		self::USER_CRASH_DATE     => 'USER_CRASH_DATE',
		self::PHONE_MODEL         => 'PHONE_MODEL',
		self::BRAND               => 'BRAND',
		self::STACK_TRACE         => 'STACK_TRACE',
		self::USER_COMMENT        => 'USER_COMMENT'
	];

	public function convert(array $get)
	{
		$appId = (int)$get[self::APP_ID];

		$package = "com.anod.car.home";
		if ($appId & 0x1 == 0x1) {
			$package .= ".pro";
		} else {
			$package .= ".free";
		}
		if ($appId & 0x10 == 0x10) {
			$package .= ".debug";
		}

		$result = self::$baseResult;
		$result["PACKAGE_NAME"] = $package;
		$result['REPORT_ID'] = self::randomUUID();
		foreach (self::$keyMap AS $key => $name) {
			$result[$name] = $get[$key];
		}

		return $result;
	}

	private static function randomUUID() {
		$data = openssl_random_pseudo_bytes(16);
		$data[6] = chr(ord($data[6]) & 0x0f | 0x40); // set version to 0100
		$data[8] = chr(ord($data[8]) & 0x3f | 0x80); // set bits 6-7 to 10

		return vsprintf('%s%s-%s-%s-%s-%s%s%s', str_split(bin2hex($data), 4));
	}


	private static $baseResult = [
		'APP_VERSION_NAME'      => null,
		'APP_VERSION_CODE'      => -1,
		'ANDROID_VERSION'       => null,
		'USER_APP_START_DATE'   => null,
		'USER_CRASH_DATE'       => null,
		'PHONE_MODEL'           => null,
		'PACKAGE_NAME'          => null,
		'BRAND'                 => null,
		'STACK_TRACE'           => null,
		'USER_COMMENT'          => null,
		'FILE_PATH'             => null,
		'PRODUCT'               => null,
		'BUILD'                 => null,
		'TOTAL_MEM_SIZE'        => null,
		'AVAILABLE_MEM_SIZE'    => null,
		'CUSTOM_DATA'           => null,
		'INITIAL_CONFIGURATION' => '',
		'CRASH_CONFIGURATION'   => '',
		'DISPLAY'               => '',
		'DUMPSYS_MEMINFO'       => '',
		'DROPBOX'               => '',
		'LOGCAT'                => '',
		'EVENTSLOG'             => '',
		'RADIOLOG'              => '',
		'IS_SILENT'             => null,
		'DEVICE_ID'             => null,
		'INSTALLATION_ID'       => null,
		'USER_EMAIL'            => null,
		'DEVICE_FEATURES'       => '',
		'ENVIRONMENT'           => '',
		'SHARED_PREFERENCES'    => '',
		'SETTINGS_SYSTEM'       => '',
		'SETTINGS_SECURE'       => '',
		'SETTINGS_GLOBAL'       => ''
	];
}
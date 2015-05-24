<?

class Adapter
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
		self::USER_COMMENT        => 'USER_COMMENT',
		self::REPORT_ID           => 'REPORT_ID'
	];

	public function convert(array $get)
	{
		$appId = (int)$get[self::APP_ID];

		$package = "com.anod.car.home";
		if ($appId & 0x1 == 0x1) {
			$package .= ".pro";
		} else if ($appId & 0x1 == 0x0) {
			$package .= ".free";
		}
		if ($appId & 0x10 == 0x10) {
			$package .= ".debug";
		}

		$result = self::$baseResult;
		$result["PACKAGE_NAME"] = $package;
		foreach (self::$keyMap AS $key => $name) {
			$result[$name] = $get[$key];
		}

		return $result;
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


$acra_format = (new Adapter())->convert($_GET);


$data_json = json_encode($acra_format);

$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, "https://anodsplace.info/acra/report/report.php");
curl_setopt($ch, CURLOPT_HTTPHEADER, array('Content-Type: application/json', 'Content-Length: ' . strlen($data_json)));
curl_setopt($ch, CURLOPT_CUSTOMREQUEST, 'PUT');
curl_setopt($ch, CURLOPT_POSTFIELDS, $data_json);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
$response = curl_exec($ch);
curl_close($ch);


?>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
	<title>Crash Report</title>

	<!-- Latest compiled and minified CSS -->
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css">
	<style>
		.container {  padding-top: 15px; }
	</style>
</head>
<body>
<div class="container">
	<div class="jumbotron">
		<h1>Report submitted</h1>
		<div class="table-responsive">
		<table class="table table-condensed">
			<tr class="warning">
				<td>Package name</td>
				<td><?= $acra_format['PACKAGE_NAME'] ?></td>
			</tr>
			<tr class="warning">
				<td>App version</td>
				<td><?= $acra_format['APP_VERSION_NAME'] ?> (<?= $acra_format['APP_VERSION_CODE'] ?>)</td>
			</tr>
			<tr class="warning">
				<td>Android version</td>
				<td><?= $acra_format['ANDROID_VERSION'] ?></td>
			</tr>
			<tr class="warning">
				<td>App start date</td>
				<td><?= $acra_format['USER_APP_START_DATE'] ?></td>
			</tr>
			<tr class="warning">
				<td>Crash date</td>
				<td><?= $acra_format['USER_CRASH_DATE'] ?></td>
			</tr>
			<tr class="warning">
				<td>Phone</td>
				<td><?= $acra_format['BRAND'] ?> <?= $acra_format['PHONE_MODEL'] ?></td>
			</tr>
			<tr class="warning">
				<td>Stack trace</td>
				<td><?= nl2br($acra_format['STACK_TRACE']) ?></td>
			</tr>
			<tr class="warning">
				<td>Comment</td>
				<td><?= $acra_format['USER_COMMENT'] ?></td>
			</tr>
		</table>
		</div>
		<p><a class="btn btn-primary btn-lg" href="javascript:close()" role="button">Close</a></p>
	</div>
</div>

</body>
</html>




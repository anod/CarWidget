<?

include("AcraAdapter.php");

$acra_format = (new AcraAdapter())->convert($_GET);

$data_json = json_encode($acra_format);

$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, "https://anodsplace.info/acra/report/report.php");
curl_setopt($ch, CURLOPT_HTTPHEADER, array('Content-Type: application/json', 'Content-Length: ' . strlen($data_json)));
curl_setopt($ch, CURLOPT_CUSTOMREQUEST, 'PUT');
curl_setopt($ch, CURLOPT_POSTFIELDS, $data_json);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
$response = curl_exec($ch);
curl_close($ch);

error_log($response);

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




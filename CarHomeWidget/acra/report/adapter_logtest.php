<?php
/**
 * @author alex
 * @date 2015-06-06
 */

include("AcraAdapter.php");

$logfile = "/var/log/httpd/ssl_access_log";

$handle = fopen($logfile, "r", false);
if ($handle) {
	while (($buffer = fgets($handle, 4096)) !== false) {
		if (strpos($buffer, "/acra/report/adapter.php") !== false) {
			$parts = explode(" ", $buffer);
			$request = trim($parts[6],'"');
			$query = parse_url($request, PHP_URL_QUERY);
			$params = [];
			parse_str($query, $params);

			$result = (new AcraAdapter())->convert($params);

			echo var_export($params, true), "\n";
			echo var_export($result, true), "\n";
		}
	}
	if (!feof($handle)) {
		echo "Error: unexpected fgets() fail\n";
	}
	fclose($handle);
}
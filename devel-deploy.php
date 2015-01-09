#!/usr/bin/php
<?php

/**
 * Settings
 */
$git = 'PlayHongKongResort';
$servers = array();
$servers[] = "104.236.139.195";
$productionDomain = 'api.hongkongresort.com';
$develDomain = 'devel-api.hongkongresort.com';

/**
 * Main Program
 */
$sourceDir = ".";
$rsyncFlag = "-rlpgoDcv";

if (basename($argv[0]) == 'devel-deploy.php')
	$isProduction = FALSE;
else
	$isProduction = TRUE;

if ($isProduction) {
	// 'git pull' to make sure you don't push outdated files to production.
	$output = `git pull`;
	if ($output != "Already up-to-date.\n")
		exit("$output\nJust did a git pull, please check your repository again. Abort.\n");

	// 'git status' to make sure you don't push uncommitted local changes to production.
	$output = `git status`;
	if ($output != "# On branch master\nnothing to commit (working directory clean)\n")
		exit("$output\nThere are uncommitted local changes. Abort.\n");
}

$localKey = exec("ssh-keyscan -t rsa localhost 2>&1 | tail -1 | awk -F' ' '{print $3;}'");

if ($isProduction)
	$destDir = '/var/www/html/'.$productionDomain;
else
	$destDir = '/var/www/html/'.$develDomain;

foreach($servers as $server) {
	echo "\n[$server]\n";

	// Check whether we are pushing to a remote host by comparing server publish ssh host key
	$destKey = exec("ssh-keyscan -t rsa $server 2>&1 | tail -1 | awk -F' ' '{print $3;}'");
	if ($localKey == $destKey)
		$isRemote = FALSE;
	else
		$isRemote = TRUE;

	echo "------------------------- rsync dryrun ----------------------\n";
	if ($isRemote)
		$cmd = "rsync -n $rsyncFlag $sourceDir/ $server:$destDir";
	else
		$cmd = "rsync -n $rsyncFlag $sourceDir/ $destDir";
	$output = `$cmd`;
	if (preg_match('/building file list \.\.\. done\s+sent \d+ bytes/', $output)) {
		echo "No file change. Skip\n";
		continue;
	}
	echo $output;
	echo "Ctrl-C to quit or any other key to proceed\n";
	system('read');

	echo "-------------------------  File push  ----------------------\n";
	if ($isRemote)
		$cmd = "rsync $rsyncFlag $sourceDir/ $server:$destDir";
	else
		$cmd = "rsync $rsyncFlag $sourceDir/ $destDir";
	system($cmd);

}

if ($isProduction && !empty($emails)) {
	$gitHash = rtrim(`git rev-parse HEAD`);
	$mailBody = <<<ENDL

[Target Directory]
$server:$destDir

[Files Rsynced]
$output
ENDL;

}

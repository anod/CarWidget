#!/usr/bin/php
<?php

declare(strict_types=1);

$opts = getopt("an:");

$files = [];
if (isset($opts['a'])) {
	$files = glob("src/main/res/layout*/sk_[a-z]*_[0-9]*.xml");
} else if (isset($opts['n'])) {
	$files = glob("src/main/res/layout*/sk_".$opts['n']."_[0-9]*.xml");
	if (!$files) {
		echo "Theme '".$opts['n']."' not found. \n";
		exit(1);
	}
} else {
	echo "Option required: 
        -a update all layouts
        -n <name> update layout with name
    ";
	exit(1);
}

foreach($files AS $path) {
	echo "Processing $path ...\n";
	$info = SkinInfo::fromPath($path);

	$row = new DOMDocument();
	$row->load($info->getRowPath());
	

	$skin = new DOMDocument();
	$skin->formatOutput = true;
	$skin->load($info->path);
	
	$skinXpath = new DOMXPath( $skin );
	$workspace = $skinXpath->query( '//LinearLayout[@android:id="@+id/workspace"]' )->item(0);
	
	// remove any child
	while ($workspace->hasChildNodes()) {
	    $workspace->removeChild($workspace->firstChild);
	}

	$rowsNum = $info->num / 2;
	for ($i = 0; $i < $rowsNum; $i++) {
		$newRow = clone $row;
		$rowXpath = new DOMXPath( $newRow );
		$text1 = $rowXpath->query( '//*[@android:id="@+id/btn_text0"]' )->item(0);
		$text2 = $rowXpath->query( '//*[@android:id="@+id/btn_text1"]' )->item(0);

		$button1 = $rowXpath->query( '//*[@android:id="@+id/btn0"]' )->item(0);
		$button2 = $rowXpath->query( '//*[@android:id="@+id/btn1"]' )->item(0);
		
		$id = $i * 2;
		$text1->setAttribute('android:id', '@+id/btn_text' . $id);
		$text2->setAttribute('android:id', '@+id/btn_text' . ($id + 1));

		$button1->setAttribute('android:id', '@+id/btn' . $id);
		$button2->setAttribute('android:id', '@+id/btn' . ($id + 1));


		$node = $skin->importNode($newRow->documentElement, true);
		
		$workspace->appendChild($node);
	}
	
	file_put_contents($info->path,$skin->saveXML());
}


class SkinInfo {
	public $path;
	public $dir;
	public $title;
	public $num;
	
	public static function fromPath(string $path): SkinInfo {
		$path_parts = pathinfo($path);
		$file_parts = explode('_',$path_parts['filename']);
		if (count($file_parts) !== 3) {
			throw new Exception ('Invalid filename: '.$path_parts['filename']);
		}
		$number = (int)$file_parts[2];
		$title = trim($file_parts[1]);
		if (!$title || !$number) {
			throw new Exception ('Invalid title/number: '.$path_parts['filename']);
		}
		$info = new SkinInfo();
		$info->path = $path;
		$info->dir = $path_parts['dirname'];
		$info->title = $title;
		$info->num = $number;
		
		return $info;
	}
	
	public function getRowPath() {
		return $this->dir.DIRECTORY_SEPARATOR.'sk_'.$this->title.'_row.xml';
	}
}

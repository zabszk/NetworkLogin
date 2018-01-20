<?php
//Authenticate for MyBB
//Copyright by Łukasz Jurczyk <zabszk [at] protonmail [dot] ch>, 2018

$servertoken = ""; //set LONG random token here

//Mozesz zaznaczyc wiele, jesli czesc userow uzywa starego hashowania
$checkMD5 = true;
$checkSHA512_bcrypt = true;
//MD5 - defaulotwe hashowanie MD5
//sha512_bcrypt - wymagane: https://github.com/dvz/mybb-dvzHash

$nonactivegroup = 5; //Grupa nieaktywowanych kont, ustaw na -1, aby wyłączyc sprawdzanie
if (empty($_POST['username']) || empty($_POST['token'])) die('Missing data');
if ($_POST['token'] != $servertoken) die('Invalid token');
require_once('../forum/inc/config.php'); //path to MyBB config
$pdo = new PDO('mysql:host=' . $config['database']['hostname'] . ';dbname=' . $config['database']['database'] . ';port=' . 3306, $config['database']['username'], $config['database']['password'], array(PDO::MYSQL_ATTR_INIT_COMMAND => "SET NAMES utf8"));
$pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
$stmt = $pdo->prepare("SELECT `password`, `salt`, `usergroup` FROM `" . $config['database']['table_prefix'] . "users` WHERE `username` = :login LIMIT 1");
$stmt->bindValue(':login', $_POST['username'], PDO::PARAM_STR);
$stmt->execute();
$row = $stmt->fetch();
if ($row == null) die ('User not found');
if ($nonactivegroup != -1 && $row['usergroup'] == $nonactivegroup) die ('Not activated');
if (empty($_POST['password'])) die('Permitted to join');
if ($checkSHA512_bcrypt) {
    $stringPrehashed = hash('sha512', $_POST['password']);
    if (password_verify($stringPrehashed, $row['password'])) die('Authenticated');
}
if ($checkMD5 && md5($row['salt'] . $_POST['password']) == $row['password']) die('Authenticated');
die ('Rejected');
//Ouput "Banned" is accepted too

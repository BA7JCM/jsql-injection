<?php
ini_set('display_errors', 1);
$db = new PDO('pgsql:host=127.0.0.1', 'postgres', 'my-secret-pw');
$array = explode(";", "SELECT '1' FROM (select 1)x where '1'={$_GET['id']}");
foreach ($array as $item) {
    foreach($db->query($item) as $row) {
        echo '<li>'. join(',', $row) .'</li>';
    }
}
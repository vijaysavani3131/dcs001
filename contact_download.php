<?php

if($_SERVER['HTTP_HOST']=="localhost"){$connect=mysqli_connect("localhost","digca76_abc","digca76_abc","digca76_abc") or die ('Database not available...');}else {$connect=mysqli_connect("localhost","u435496484_card","India@123@","u435496484_card") or die ('Connection issue #567845 Error');}


$query=mysqli_query($connect,'SELECT * FROM digi_card WHERE id="'.$_GET['id'].'" ');

	$row=mysqli_fetch_array($query);


header('Content-Type:text/x-vcard');  
  header('Content-Disposition: inline');  

?>
 BEGIN:VCARD
VERSION:3.0
PRODID:-//Apple Inc.//Mac OS X 10.14.1//EN
N:<?php echo $row['d_l_name']; ?>;<?php echo $row['d_f_name']; ?>;;;
FN:<?php echo $row['d_f_name'].$row['d_l_name']; ?> 
ORG:<?php echo $row['d_comp_name']; ?>;
TITLE:<?php echo $row['d_comp_name']; ?> 
EMAIL;type=INTERNET;type=WORK;type=pref:<?php echo $row['d_email']; ?> 
TEL;type=WORK;type=pref:<?php echo $row['d_contact']; ?> 
TEL;type=CELL:<?php echo $row['d_contact2']; ?> 
TEL;type=HOME:<?php echo $row['d_whatsapp']; ?> 
URL;type=pref:https://<?php echo $_SERVER['HTTP_HOST'].'/'.$row['card_id']; ?> 

END:VCARD


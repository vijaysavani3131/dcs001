<?php
//get data from form  

$name = $_POST['user_name'];
$mobile= $_POST['user_contact'];
$email= $_POST['user_email'];
$message= $_POST['message'];
$to = "info@v-card.in";
$subject = "Mail From website";
$txt ="Name = ". $name . "\r\n Mobile = " . $mobile ."\r\n Email = " . $mail . "\r\n Message =" . $message;
$headers = "From: isavgotechnologies@gmail.com
" . "\r\n" .
"CC: isavgotechnologies@gmail.com
";
if($email!=NULL){
    mail($to,$subject,$txt,$headers);
}
//redirect
header("Location:index.php");
?>
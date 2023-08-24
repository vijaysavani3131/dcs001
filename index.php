<title> Isavgo || Digital Visiting Card</title>

<head>


<link rel="favicon" href="images/logo.png" type="image/*">
<link rel="fav-icon" href="images/logo.png" type="image/*">
<link rel="icon" href="images/logo.png" type="image/*">

 <meta name="keywords" content="digital business card, digital business card app, business card app, best digital business card app, business card app for iPhone, business card app for android, free business card, Digital Visiting Card ,Digital Visiting Card Online, Business Card Online, Visiting card, v card">
 
 <meta name="description" content="Best digital visiting card online with many designs, Now create in just 5 minutes and get it instantly.">
  <!-- Required meta tags -->
  <meta charset="utf-8" />
  <link rel='stylesheet' href='panel/all.css' integrity='sha384-lZN37f5QGtY3VHgisS14W3ExzMWZxybE1SJSEsQp9S+oqd12jhcu+A56Ebc1zFSJ' crossorigin='anonymous'>
  <meta name="google-site-verification" content="RjyVv4ISQhKLzqEfKPe3fFENreEjM1lMHeCE5r99hMs" />
  <link rel="stylesheet" href="panel/awesome.min.css">
  <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
 <link href='https://fonts.googleapis.com/css?family=Amita' rel='stylesheet'>
 <meta      name='viewport'      content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0' />

<link rel="stylesheet" href="css2.css" >
<link rel="stylesheet" href="mobile_css.css" >
<script src="master_js.js"></script>






</head>
<meta name="apple-mobile-web-app-capable" content="yes">
<meta name="apple-mobile-web-app-title" content="Digital Visiting Card">

<meta name="apple-mobile-web-app-status-bar-style" content="black">

<link rel="apple-touch-icon" sizes="192x192" href="images/logo192.png" type="image/png">
<link rel="apple-touch-icon" sizes="512x512" href="images/logo512.png" type="image/png">
<link rel="mask-icon" href="images/logo192.png" color="#5bbad5">

<link rel="manifest" href="manifest.json">     
<meta name="msapplication-TileColor" content="#ffffff">
<meta name="msapplication-TileImage" content="logo.png">
<meta name="theme-color" content="#ffffff"> 

<script type="text/javascript">
 if ('serviceWorker' in navigator) {
    console.log("Will the service worker register?");
    navigator.serviceWorker.register('service-worker.js')
      .then(function(reg){
        console.log("Yes, it did.");
     }).catch(function(err) {
        console.log("No it didn't. This happened:", err)
    });
 };
 
 </script>
 <script>
 
let deferredPrompt;


window.addEventListener('beforeinstallprompt', (e) => {
	var buttonInstall=document.getElementById("buttonInstall");
	
console.log("Nn");
  // Prevent the mini-infobar from appearing on mobile
  e.preventDefault();
  // Stash the event so it can be triggered later.
  deferredPrompt = e;
  // Update UI notify the user they can install the PWA
  
			  buttonInstall.addEventListener('click', (e) => {
			  // Hide the app provided install promotion
			  buttonInstall.style.display="none";
			  
			  // Show the install prompt
			  deferredPrompt.prompt();
			  // Wait for the user to respond to the prompt
			  deferredPrompt.userChoice.then((choiceResult) => {
				if (choiceResult.outcome === 'accepted') {
				  console.log('User accepted the install prompt');
				} else {
				  console.log('User dismissed the install prompt');
				  buttonInstall.style.display="flex";
				}
			  });
			});
});

window.addEventListener('appinstalled', (evt) => {
  // Log install to analytics
  console.log('INSTALL: Success');
  var buttonInstall=document.getElementById("buttonInstall");
  buttonInstall.style.display="none";
});


</script>


	<script>

	

function closeLoader(){
	console.log('yes');
	$('.card_loader_back').hide();
}

setTimeout(closeLoader,3000);

</script>




<style>
 
    
</style>
<body onload="closeLoader()">

<header id="header">
	<div class="logo">
	<h3><img src="images/logo.png">  	<!--<div class="text_logo">apnacards.com</div>--</h3>
	</div>
	<div class="mobile_home">&equiv;</div>
	<div class="head_txt">
		<div class="mobile_close">
			&times;
		</div>
		
	<!--	<a href="panel/franchisee-login/login.php"><h3 class="login_btn">Franchisee Login </h3></a> -->
		<a href="https://api.whatsapp.com/send?phone=917573003890&text=I%20am%20Interested%20For%20Digital%20Visiting%20Card.%20Please%20Share%20Me%20Demo&source=&data=&app_absen"><h3 class="login_btn">Business With Us</h3></a>
		<a href="#contact"><h3 class="login_btn" >Contact Us</h3></a>
		<a href="panel/login/login.php"><h3 class="login_btn">Customer Login</h3></a>
	</div>
	

</header>

<!--------search box-----
			<div class="search_box">
				<div class="close" onclick="close_search()">&times;</div>
				<h2>Search here <i class="fa fa-binoculars"></i></h2>
				<input type="search" id="search_val" placeholder="Search for professionals/Business/Developers etc...">
				<button id="search_me" onclick="searchMe()"><i class="fa fa-search"></i> Search Now</button>
					<div id="search_result">
						
					</div>
			</div>
			--------->
			
<script>

$(document).ready(function(){
	$('.mobile_home,.mobile_close').on('click',function(){
		$('.head_txt').slideToggle(300);
		
	})
	
	

})
function closeNoti() {
		$('.notification').toggle();
	}
	
function close_search() {
		$('.search_box').toggle();
	}
	
function searchMe(){
	var search=$('#search_val').val();
	if(search.length > 2){
		$.ajax({
			url:'search.php',
			method:'POST',
			data:{search:search},
			dataType:'text',
			success:function(data){
				$('#search_result').html(data);
				
				}
			});
	}
		
										
}
</script>

<div class="main" style="">
	<div class="clip_path1"></div>
	
	<img src="images/back5.png" id="background_img">
	
	<div class="main_txt">
		<h1>Digital Business Cards: The Future of Professional Networking</h1>
		<p>Experience seamless, user-friendly digital business card creation with our innovative platform</p>
		<p>Craft your perfect solution in just 5 minutes</p>
		
		
		<a href="panel/login/login.php" target="_blank"><div class="btn_2">Create Now <i class="fa fa-caret-right"></i></div></a>
	</div>
		<div class="demo_card_side">
		<img src="panel/images/template32.png">
	</div>
	


</div>
	

<!-------------search ---------

<div class="search">
	<div class="search_result"></div>
	<input type="search" name="" id="search" ><input type="submit">
	
</div>

<!--------------------------------------------->
<div class="row display_flex" >
	
	<div class="side2">
		
		
		<h1>Say Goodbye to Outdated Paper Business Cards</h1>
		
		<h3>
				Design your digital business card in no time - it's fast, sophisticated, and complimentary. This digital card is perpetually in your possession, never deteriorates, and never goes out of stock. Conveniently update your digital business card through our easy-to-use interface, ensuring you never have to deal with reprints again.</h3>

		
	</div>

	<div class="side1">
	
	<img src="panel/images/template24.png" id="imgd1">
	<img src="panel/images/template23.png" id="imgd2">
	<img src="panel/images/template26.png" id="imgd3"> 
	
		
	</div>
	
	
</div>

<div class="wtsp_chat">
	<a href="https://api.whatsapp.com/send?phone=917573003890&text=I%20am%20Interested%20For%20Digital%20Visiting%20Card.%20Please%20Share%20Me%20Demo&source=&data=&app_absent=" target="_blank"><img src="images/wtsp.png" alt="Live Chat"></a>
</div>

<div class="row row_backimg">

	<h1>How It Works?</h1>
	
	<div class="flex_box">
		<div class="flex_boxin">
		<i class="fa fa-edit"></i>
		<h1>1. Create your Card</h1>
		<p>Design your digital visiting card in 2 minutes</p>
		
		</div>
		<div class="flex_boxin">
		<i class="fa fa-download"></i>
		<h1>2. Save to your Device</h1>
		<p>Digital Visiting Card is accessible anytime from anywhere</p>
		
		</div>
		<div class="flex_boxin">
		<i class="fa fa-share-alt"></i>
		<h1>3. Share with friends and colleagues</h1>
		<p>through a variety of channels</p>
		
		</div>
	</div>
	
	
</div>



<div class="row2">

		
	<div class="flex_pricing">
		
		<div class="flex_pricingin">
			<h3>Digital Card with Ecommerce </h3>
			<h1>
			<del>₹ 2999</del> Now @ just ₹999 Only<p> </p></h1>
			
			
			
			<ul>
				<li ><i class="fa fa-check"></i> 35 Premium themes</li>
				<li class="back"><i class="fa fa-check"></i> Share cards with anyone, Unlimited times</li>
			
				<li ><i class="fa fa-check"></i> Update card Unlimited times.</li>
				<li class="back" ><i class="fa fa-check"></i> Feedback option available.</li>
				<li ><i class="fa fa-check"></i> 20 Products in Ecommerce Store </li>
				<li class="back"><i class="fa fa-check"></i> Profile Photo</li>
				<li><i class="fa fa-check"></i> Select design from available templates</li>
				<li class="back"><i class="fa fa-check"></i> 10 Products or Services</li>
				<li><i class="fa fa-check"></i> 10 Photos in Gallery</li>
				<li class="back"> <i class="fa fa-check"></i> Social Media Links</li>
				<li > <i class="fa fa-check"></i> 5 Videos in Youtube Video Gallery</li>
				<li class="back"><i class="fa fa-check"></i> Products with images.</li>
				<li><i class="fa fa-check"></i> Payment Section</li>
				<li  class="back"><i class="fa fa-check"></i> Contact Form Included</li>
			</ul>
			<a href="panel/login/login.php"><div class="btn_1">Create Now Online <i class="fa fa-caret-right"></i></div></a>
			</div>
		
		
	</div>
	
	
</div>

<h1 style="text-align: center;
    font-weight: 600;">Sample vCard Templates. </h1>
<div class="temp_preview">

	
	<div class="demo_slider">
	    
	  	<div class="temp_pre"><div class="new_lab">51Exclusive</div><img src="panel/images/template31.png"></div>
	    	
	<div class="temp_pre"><div class="new_lab">51Exclusive</div><img src="panel/images/template32.png"></div>

	<div class="temp_pre"><div class="new_lab">51Exclusive</div><img src="panel/images/template33.png"></div>

	<div class="temp_pre"><div class="new_lab">51Exclusive</div><img src="panel/images/template34.png"></div>

	<div class="temp_pre"><div class="new_lab">51Exclusive</div><img src="panel/images/template35.png"></div>

		<div class="temp_pre"><div class="new_lab">New</div><img src="panel/images/template1.png"></div>
		<div class="temp_pre"><div class="new_lab">New</div><img src="panel/images/template2.png"></div>
		<div class="temp_pre"><div class="new_lab">New</div><img src="panel/images/template3.png"></div>
		<div class="temp_pre"><div class="new_lab">New</div><img src="panel/images/template4.png"></div>
		<div class="temp_pre"><div class="new_lab">New</div><img src="panel/images/template5.png"></div>
		<div class="temp_pre"><div class="new_lab">New</div><img src="panel/images/template6.png"></div>
		<div class="temp_pre"><div class="new_lab">New</div><img src="panel/images/template7.png"></div>
		<div class="temp_pre"><div class="new_lab">New</div><img src="panel/images/template8.png"></div>
		<div class="temp_pre"><div class="new_lab">New</div><img src="panel/images/template9.png"></div>
		<div class="temp_pre"><div class="new_lab">New</div><img src="panel/images/template10.png"></div>
		<div class="temp_pre"><div class="new_lab">New</div><img src="panel/images/template11.png"></div>
		<div class="temp_pre"><div class="new_lab">New</div><img src="panel/images/template12.png"></div>
		<div class="temp_pre"><div class="new_lab">New</div><img src="panel/images/template13.png"></div>
		<div class="temp_pre"><div class="new_lab">New</div><img src="panel/images/template29.png"></div>	
		<div class="temp_pre"><div class="new_lab">New</div><img src="panel/images/template30.png"></div>
		<div class="temp_pre"><img src="panel/images/template14.png"></div>
		<div class="temp_pre"><img src="panel/images/template15.png"></div>
		
		<div class="temp_pre"><img src="panel/images/template16.png"></div>
		<div class="temp_pre"><img src="panel/images/template17.png"></div>
		<div class="temp_pre"><img src="panel/images/template18.png"></div>
		<div class="temp_pre"><img src="panel/images/template19.png"></div>
		<div class="temp_pre"><img src="panel/images/template20.png"></div>
		<div class="temp_pre"><img src="panel/images/template21.png"></div>
		<div class="temp_pre"><img src="panel/images/template22.png"></div>
		<div class="temp_pre"><img src="panel/images/template23.png"></div>

		<div class="temp_pre"><img src="panel/images/template24.png"></div>
		<div class="temp_pre"><img src="panel/images/template25.png"></div>
		<div class="temp_pre"><img src="panel/images/template26.png"></div>
		<div class="temp_pre"><img src="panel/images/template27.png"></div>
		<div class="temp_pre"><img src="panel/images/template28.png"></div>
	
	</div>


</div>


<!-----------Youtube video--------------------->


	

<!--<center><iframe width="500" height="250" src="https://www.youtube.com/embed/uOPcRGevVCA"></iframe> </center> </br>-->


<!---------------beneficial for-------------------------->

<div class="row33">
	<h1>Digital Cards Offer Benefits For</h1>
	<p>If you interact with potential clients directly (during one-on-one sessions) or make use of telephonic discussions to advertise and sell your goods or services, our Digital Business Card can help share your message more strategically.</p>
	
	
	
	<div class="benefit_box">
		<i class="fa fa-briefcase"></i>
		<h3>Business Owners</h3>
		<p>Business owners who call and/or meet prospects personally to get business.</p>
	</div>
	
	<div class="benefit_box">
		<i class="fa fa-handshake-o"></i>
		<h3>Sales Professionals</h3>
		<p>Independent Sales Professionals, Field Staff and Sales Executives.</p>
	</div>
	
	<div class="benefit_box">
		<i class="fa fa-sellsy"></i>
		<h3>Software & IT</h3>
		<p>Web Designers, Digital and Social Media Marketers who call / meet business people.</p>
	</div>

	<div class="benefit_box">
		<i class="fa fa-bullhorn"></i>
		<h3>Marketing Agencies</h3>
		<p>Being a Digital marketer is one thing and being a Expert in it is another. We have dedicated staff handling each field of Digital Marketing.</p>
	</div>
	
	<div class="benefit_box">
		<i class="fa fa-black-tie"></i>
		<h3>Consultants</h3>
		<p>Architect, Interior Designers, CA, Finance and other Consultants.</p>
	</div>

	<div class="benefit_box">
		<i class="fa fa-plane"></i>
		<h3>Events and Travels</h3>
		<p>Professionals from Event Management, Tours and Travel Companies.</p>
	</div>
	<div class="benefit_box">
		<i class="fa fa-bar-chart"></i>
		<h3>Real Estate & Realtors</h3>
		<p>We provide you 100% legal and RERA Registered projects with satisfactory of your choice and in your selected locations and your budget –.</p>
	</div>	
	<div class="benefit_box">
		<i class="fa fa-graduation-cap"></i>
		<h3>Education & Training</h3>
		<p>Corporate Trainers, Educational Workshops, HR Consultants and Teachers.</p>
	</div>
	<div class="benefit_box">
		<i class="fa fa-heartbeat"></i>
		<h3>Health and Beauty</h3>
		<p>Gym, Beautician, Salon, Dietician, Image Consultants Yoga & Dance Professionals.</p>
	</div>

</div>

<!---------------beneficial for--------------------->
<div class="row_features">
<p>FEATURES</p>
	<h1>A Single Digital Card, Infinite Opportunities</h1>
	
	<div class="cont_share_boxes"><i class="fa fa-phone"></i> One CLick Call</div>

	<div class="cont_share_boxes"><i class="fa fa-whatsapp"></i> One CLick WhatsApp</div>
	<div class="cont_share_boxes"><i class="fa fa-envelope"></i> One Click Email</div>
	<div class="cont_share_boxes"><i class="fa fa-star"></i> Get Customers Feedback</div>
	<div class="cont_share_boxes"><i class="fa fa-map-marker"></i> One CLick Navigate</div>
	<div class="cont_share_boxes"><i class="fa fa-mobile"></i> Add to Contacts</div>
	<div class="cont_share_boxes"><i class="fa fa-facebook"></i> Website & Social Links</div>
	<div class="cont_share_boxes"><i class="fa fa-share-alt"></i> Share Unlimited</div>
	<div class="cont_share_boxes"><i class="fa fa-bank"></i> Online Store</div>
	<div class="cont_share_boxes"><i class="fa fa-edit"></i> Easy To Update</div>
	<div class="cont_share_boxes"><i class="fa fa-image"></i> Photo Gallery</div>
	<div class="cont_share_boxes"><i class="fa fa-youtube"></i> Youtube Video Gallery</div>
	<div class="cont_share_boxes"><i class="fa fa-rupee"></i> Payment Section</div>
	<div class="cont_share_boxes"><i class="fa fa-comment"></i> Enquiry Form</div>

		


</div>

<h2 id="servicesh2" >Services We Offer</h2>
<div class="services" id="services">
	<div class="scroll_cont">

	<div class="services_box" onclick="location.href='https://api.whatsapp.com/send?phone=917573003890&text=I%20am%20Interested%20For%20Digital%20Visiting%20Card.%20Please%20Share%20Me%20Demo&source=&data=&app_absent='" target="_blank"><h3>Digital Visiting Card</h3><p>Still using old boring visiting cards? Now its time to change.</p><div class="pre_btn">Select</div></div>
	<div class="services_box" onclick="location.href='https://api.whatsapp.com/send?phone=917573003890&text=I%20am%20Interested%20For%20Digital%20Visiting%20Card.%20Please%20Share%20Me%20Demo&source=&data=&app_absent='" target="_blank"><h3>Website Design</h3><p>Design your own Business/Professional website.</p><div class="pre_btn">Select</div></div>
	<div class="services_box" onclick="location.href='https://api.whatsapp.com/send?phone=917573003890&text=I%20am%20Interested%20For%20Digital%20Visiting%20Card.%20Please%20Share%20Me%20Demo&source=&data=&app_absent='" target="_blank"><h3>Digital Marketing</h3><p>Advertise online and expand your business. </p><div class="pre_btn">Select</div></div>
	<div class="services_box" onclick="location.href='https://api.whatsapp.com/send?phone=917573003890&text=I%20am%20Interested%20For%20Digital%20Visiting%20Card.%20Please%20Share%20Me%20Demo&source=&data=&app_absent='" target="_blank"><h3>Logo Designing</h3><p>Best logo disigners available to design your business logo.</p><div class="pre_btn">Select</div></div>
	

	</div>
</div>



<div class="row_bottom display_flex" id="contact">

	<div class="side1">
		
		
		<h1>My Digital Card</h1>
		

		
		<div class="row_bt_p"><i class="fa fa-phone"></i> <h4>+917573003890</h4></div>
		<div class="row_bt_p"><i class="fa fa-envelope"></i><h4>info@isavgo.com</h4></div>
		
		
		
		
		<div class="row_bt_p"><iframe src="https://www.google.com/maps/embed?pb=!1m14!1m8!1m3!1d14872.200394872501!2d72.9678214!3d21.2694844!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x3be0474df7bff0d5%3A0xa79a1ebb7b6b3ea7!2sIsavgo%20Technologies%20Pvt%20Ltd!5e0!3m2!1sen!2sin!4v1682015460824!5m2!1sen!2sin" width="600" height="450" style="border:0;" allowfullscreen="" loading="lazy" referrerpolicy="no-referrer-when-downgrade"></iframe></div>
		
	</div>
	<div class="side2">
	<h3>CONTACT US</h3>
		<form action="mail.php" method="Post">
			<input type="" name="user_name" placeholder="Enter your name" required>
			<input type="" name="user_contact" placeholder="Enter contact number" required>
			<input type="" name="user_email" placeholder="Enter email" required>
			<textarea name="user_msg" placeholder="Enter your query" required></textarea>
			<input type="submit" value="Send" name="send_email">
		
		
		</form>
		
		


	</div>
	
</div>



<footer class="">
    
    <p><a href="https://www.isavgo.com">Privacy Policy</a> &nbsp; ||  <a href="https://www.isavgo.com">Return And Refund</a> &nbsp; || <a href="https://www.isavgo.com">Contact Us</a></p><br> 
    
    <p style="color: white;">2023 || Website Developed And Managed By <b> <a href="https://dwww.isavgo.com" style="color: blue;">Isavgo Technologies</a></b></p><br>

</footer>

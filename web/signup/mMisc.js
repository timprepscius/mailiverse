
function getPasswordStrength (password)
{
	 var score = 0;
	 var suggest = [];
	 
	 //if password bigger than 6 give 1 point
	 if (password.length > 6) 
		 score++;		 
	 else
		 suggest.push("Should be longer than 6 characters.");

	 //if password has at least one number give 1 point
	 if (password.match(/\d+/)) 
		 score++;
	 else
		 suggest.push("Consider adding a number.");

	 //if password has both lower and uppercase characters give 1 point 
	 if ( ( password.match(/[a-z]/) ) && ( password.match(/[A-Z]/) ) )
		 score++;
	 else
		 suggest.push("Passwords with upper AND lower case are stronger.");


	 //if password has at least one special caracther give 1 point
	 if ( password.match(/.[!,@,#,$,%,^,&,*,?,_,~,-,(,)]/) ) 
		 score++;
	 else
		 suggest.push("Use a non alpha numeric character");
		 
	 //if password bigger than 12 give another 1 point
	 if (password.length > 12) score++;
	 
	 var texts = [ "Very weak", "Weak", "Low", "Medium", "Strong", "Good"];
	 return {
		 score: score,
		 text: texts[score],
		 suggest: (suggest.length ? suggest[0] : "")
	 };
}

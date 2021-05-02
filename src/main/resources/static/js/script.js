console.log("this is script file");

// Creating Sidebar dynamic
const toggleSidebar=()=>{

    if($('.sidebar').is(":visible")){

        $(".sidebar").css("display","none");
        $(".content").css("margin-left","0%");

    }else{
        
        $(".sidebar").css("display","block");
        $(".content").css("margin-left","20%");
    }
};

// Creating Search Function
const search=()=>{
    // console.log("searching");
    let query=$("#search-input").val();
    
    
    if(query==""){
        $(".search-result").hide();
    }else{
        console.log(query);

        // Sending Request to Server
        let url = `http://localhost:8080/search/${query}`;

        fetch(url).then((response)=> {
            return response.json();
        }).then((data)=> {
            console.log(data);
            let result = `<div class="list-group">`
            data.forEach((contact) => {
                result += `<a href="/user/${contact.cid}/contact" class="list-group-item list-group-action">${contact.name}</a>`
            });
            result += `</div>`;
            $(".search-result").html(result);
            $(".search-result").show();
        });
        
    }
}

$(document).ready(function(){
$('.pass_show').append('<span class="ptxt">Show</span>');  
});
  

$(document).on('click','.pass_show .ptxt', function(){ 

$(this).text($(this).text() == "Show" ? "Hide" : "Show"); 

$(this).prev().attr('type', function(index, attr){return attr == 'password' ? 'text' : 'password'; }); 

});

function parseUser(formName) {
    let user = {
        id: '',
        firstname: $("#firstname" + formName).val(),
        lastname: $("#lastname" + formName).val(),
        age: $("#age" + formName).val(),
        email: $("#email" + formName).val(),
        username: $("#username" + formName).val(),
        password: $("#password" + formName).val(),
        roleStr: ''
    };
    if ($('#id' + formName).length) {
        user.id = $("#id" + formName).val()
    }
    if ($("#checkbox" + formName + "-1").length) {
        if (document.getElementById("checkbox" + formName + "-1").checked) user.roleStr += "ADMIN";
        if (document.getElementById("checkbox" + formName + "-2").checked) user.roleStr += "USER";
        if (document.getElementById("checkbox" + formName + "-3").checked) user.roleStr += "GUEST";
    }
    return user;
}

function setUser(user, formName) {
    $("#id"+formName).val(user.id);
    $("#firstname"+formName).val(user.firstname);
    $("#lastname"+formName).val(user.lastname);
    $("#age"+formName).val(user.age);
    $("#email"+formName).val(user.email);
    $("#username"+formName).val(user.username);
    $("#password"+formName).val(user.password);
    if (user.userRoleStr.includes("ADMIN")) document.getElementById("checkbox"+formName+"-1").checked=true;
    if (user.userRoleStr.includes("USER")) document.getElementById("checkbox"+formName+"-2").checked=true;
    if (user.userRoleStr.includes("GUEST")) document.getElementById("checkbox"+formName+"-3").checked=true;
}


function disableButton(btnName){
    document.getElementById(btnName).setAttribute('disabled','disabled');
    document.getElementById(btnName).classList.remove('btn-info');
    document.getElementById(btnName).classList.add('btn-outline-secondary','border-white','border-top');
}
function enableButton(btnName){
    document.getElementById(btnName).removeAttribute('disabled');
    document.getElementById(btnName).classList.add('btn-info');
    document.getElementById(btnName).classList.remove('btn-outline-secondary','border-white','border-top');
}

async function loadPage(link) {
    let response = await fetch('/api/'+link);
    let html = await response.text();

    html.includes("<title>Please sign in</title>") ? window.location.replace('/login') : $("#mainBlock").html(html);

    if (link==="admin") {
        await update();
    }
}
function clearUserFields(formName) {
    $("#firstname" + formName).val('')
    $("#lastname" + formName).val('')
    $("#age" + formName).val('')
    $("#email" + formName).val('')
    $("#username" + formName).val('')
    $("#password" + formName).val('')

    if ($('#id' + formName).length) {
        $("#id" + formName).val('')
    }
    if ($("#checkbox" + formName + "-1").length) {
        document.getElementById("checkbox" + formName + "-1").checked = false;
        document.getElementById("checkbox" + formName + "-2").checked = false;
        document.getElementById("checkbox" + formName + "-3").checked = false;
    }
    clearErrorFields(formName);
}

function setErrorFields(userErrorText, formName) {
    if ($("#id" + formName + "Error").length) {
        $("#id" + formName + "Error").html(userErrorText.id);
    }

    $("#username" + formName + "Error").html(userErrorText.username);
    if (userErrorText.username !== '') {
        $("#username" + formName).removeClass('bg-white').css("background-color", "#ffebb9");
    } else $("#username" + formName).addClass('bg-white');

    $("#password" + formName + "Error").html(userErrorText.password);
    if (userErrorText.password !== '') {
        $("#password" + formName).removeClass('bg-white').css("background-color", "#ffebb9");
    } else $("#password" + formName).addClass('bg-white');

    $("#email" + formName + "Error").html(userErrorText.email);
    document.getElementById("email" + formName + "Error").innerText = userErrorText.email;
    if (userErrorText.email !== '') {
        $("#email" + formName).removeClass('bg-white').css("background-color", "#ffebb9");
    } else $("#email" + formName).addClass('bg-white');

    $("#firstname" + formName + "Error").html(userErrorText.firstname);
    if (userErrorText.firstname !== '') {
        $("#firstname" + formName).removeClass('bg-white').css("background-color", "#ffebb9");
    } else $("#firstname" + formName).addClass('bg-white');

    $("#lastname" + formName + "Error").html(userErrorText.lastname);
    if (userErrorText.lastname !== '') {
        $("#lastname" + formName).removeClass('bg-white').css("background-color", "#ffebb9");
    } else $("#lastname" + formName).addClass('bg-white');

    $("#age" + formName + "Error").html(userErrorText.age);
    document.getElementById("age" + formName + "Error").innerText = userErrorText.age;
    if (userErrorText.age !== '') {
        $("#age" + formName).removeClass('bg-white').css("background-color", "#ffebb9");
    } else $("#age" + formName).addClass('bg-white');
}

function clearErrorFields(formName) {
    function clearErrorField(field, formName) {
        if ($("#" + field + formName + "Error").length) {
            $("#" + field + formName + "Error").html('');
            $("#" + field + formName).addClass('bg-white');
        }
    }
    clearErrorField("id", formName);
    clearErrorField("username", formName);
    clearErrorField("password", formName);
    clearErrorField("email", formName);
    clearErrorField("firstname", formName);
    clearErrorField("lastname", formName);
    clearErrorField("age", formName);
}
async function fazerLogin() {
    const user = document.getElementById('usuario').value;
    const pass = document.getElementById('senha').value;
    const msg = document.getElementById('mensagem');

    // Por enquanto, validação local. Depois seu colega de banco integra aqui!
    if (user === "admin" && pass === "123") {
        msg.style.color = "green";
        msg.innerText = "Acessando...";

        // Simula um pequeno delay e redireciona
        setTimeout(() => {
            window.location.href = "dashboard.html";
        }, 1000);
    } else {
        msg.innerText = "Usuário ou senha incorretos!";
    }
}
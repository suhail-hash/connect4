document.querySelectorAll("form").forEach(form => {
    form.addEventListener("submit", function() {
        const btn = this.querySelector("button");
        btn.disabled = true;
        btn.innerText = "Please wait...";
    });
});
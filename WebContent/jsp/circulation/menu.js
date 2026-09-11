document.addEventListener("DOMContentLoaded", function () {
  const toggles = document.querySelectorAll(".tree-toggle");

  toggles.forEach(function (btn) {
    const icon = btn.querySelector(".icon");
    const target = document.querySelector(btn.dataset.bsTarget);

    // Quando for abrir
    target.addEventListener("show.bs.collapse", function () {
      // Fecha todos os outros abertos
      document.querySelectorAll(".children.show").forEach(function (open) {
        if (open !== target) {
          const collapseInstance = bootstrap.Collapse.getOrCreateInstance(open);
          collapseInstance.hide();
        }
      });

      icon.textContent = "−";
      btn.setAttribute("aria-expanded", "true");
    });

    // Quando fechar
    target.addEventListener("hide.bs.collapse", function () {
      icon.textContent = "+";
      btn.setAttribute("aria-expanded", "false");
    });
  });

  document.querySelectorAll(".leaf").forEach(function (leaf) {
    leaf.addEventListener("click", function () {
      alert("Clicado: " + this.textContent.trim());
    });
  });
});

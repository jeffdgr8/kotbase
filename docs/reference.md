---
hide:
  - navigation
  - toc
---

<script>

const container = document.querySelector(".md-container");
const observer = new MutationObserver((mutationsList) => {
    for (const mutation of mutationsList) {
        if (mutation.type === 'childList') {
            mutation.addedNodes.forEach((addedNode) => {
                if (addedNode.classList && addedNode.classList.contains("md-footer")) {
                    addedNode.remove();
                    observer.disconnect();
                }
            });
        }
    }
});
observer.observe(container, { childList: true });

const content = document.querySelector(".md-content");
content.remove();

const iframe = document.createElement("iframe");
iframe.src = "/api/index.html";
iframe.style.border = "none";
iframe.style.width = "100%";
iframe.style.flexGrow = "1";

const mainGrid = document.querySelector(".md-main__inner.md-grid");
mainGrid.style.margin = 0;

const main = document.querySelector(".md-main");
main.style.display = "flex";
main.style.flexDirection = "column";
main.append(iframe)

</script>

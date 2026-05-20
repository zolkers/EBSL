import { queryViewerElements } from "./dom.js";
import { ReplayController } from "./replay-controller.js";

if ("serviceWorker" in navigator) {
  navigator.serviceWorker.register("sw.js").catch(() => {
    // Offline install is optional; the viewer remains fully usable without it.
  });
}

new ReplayController(queryViewerElements()).start();

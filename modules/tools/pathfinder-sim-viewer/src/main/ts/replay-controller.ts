import type { ViewerElements } from "./dom.js";
import { clamp, normalizeRadians } from "./math-utils.js";
import { parseReplay } from "./replay-model.js";
import { ReplayRenderer } from "./replay-renderer.js";
import { createViewerState, maxFrame, resetCamera, TICK_MS, ViewerState } from "./viewer-state.js";

const WHEEL_ZOOM_FACTOR = 1.12;
const ROTATION_RADIANS_PER_PIXEL = 0.008;

export class ReplayController {
  private readonly state: ViewerState = createViewerState();
  private readonly renderer: ReplayRenderer;

  constructor(private readonly elements: ViewerElements) {
    this.renderer = new ReplayRenderer(elements.canvas);
  }

  start(): void {
    this.elements.fileInput.addEventListener("change", () => void this.loadSelectedFile());
    this.elements.timeline.addEventListener("input", () => this.setFrame(Number(this.elements.timeline.value)));
    this.elements.playButton.addEventListener("click", () => this.togglePlayback());
    this.elements.resetViewButton.addEventListener("click", () => this.resetView());
    this.elements.modeButton.addEventListener("click", () => this.toggleMode());
    this.elements.canvas.addEventListener("pointerdown", event => this.startGesture(event));
    this.elements.canvas.addEventListener("pointermove", event => this.updateGesture(event));
    this.elements.canvas.addEventListener("pointerup", event => this.endGesture(event));
    this.elements.canvas.addEventListener("pointercancel", event => this.endGesture(event));
    this.elements.canvas.addEventListener("wheel", event => this.zoomWithWheel(event), { passive: false });
    this.elements.dropZone.addEventListener("dragover", event => this.allowDrop(event));
    this.elements.dropZone.addEventListener("dragleave", () => this.leaveDrop());
    this.elements.dropZone.addEventListener("drop", event => void this.dropFile(event));
    window.addEventListener("keydown", event => this.handleKeyboard(event));
    window.addEventListener("resize", () => this.render());
    this.render();
  }

  private async loadSelectedFile(): Promise<void> {
    const file = this.elements.fileInput.files?.[0];
    if (file !== undefined) {
      await this.loadReplayFile(file);
    }
  }

  private async loadReplayFile(file: File): Promise<void> {
    this.state.result = parseReplay(JSON.parse(await file.text()));
    this.state.frame = 0;
    this.elements.timeline.max = maxFrame(this.state).toString();
    this.elements.timeline.value = "0";
    resetCamera(this.state);
    this.updateUi();
  }

  private togglePlayback(): void {
    this.state.playing = !this.state.playing;
    this.elements.playButton.textContent = this.state.playing ? "Pause" : "Play";
    this.stopTimer();
    if (this.state.playing) {
      this.state.timer = window.setInterval(() => this.stepPlayback(), TICK_MS);
    }
  }

  private stepPlayback(): void {
    if (this.state.result === null || this.state.frame >= maxFrame(this.state)) {
      this.state.playing = false;
      this.elements.playButton.textContent = "Play";
      this.stopTimer();
      return;
    }
    this.setFrame(this.state.frame + 1);
  }

  private setFrame(frame: number): void {
    this.state.frame = clamp(frame, 0, maxFrame(this.state));
    this.elements.timeline.value = this.state.frame.toString();
    this.updateUi();
  }

  private toggleMode(): void {
    this.state.mode = this.state.mode === "3d" ? "2d" : "3d";
    this.elements.modeButton.textContent = this.state.mode.toUpperCase();
    this.render();
  }

  private resetView(): void {
    resetCamera(this.state);
    this.render();
  }

  private updateUi(): void {
    const tick = this.state.result?.trace[this.state.frame];
    this.elements.emptyState.classList.toggle("hidden", this.state.result !== null);
    this.elements.scenarioLabel.textContent = this.state.result?.scenarioId ?? "No replay loaded";
    this.elements.frameLabel.textContent = tick === undefined ? "tick 0" : `tick ${tick.tick}`;
    this.elements.metricStatus.textContent = this.state.result?.status ?? "-";
    this.elements.metricMove.textContent = tick?.moveType ?? "-";
    this.elements.metricDistance.textContent = tick === undefined ? "-" : tick.distanceToGoal.toFixed(2);
    this.elements.metricStuck.textContent = tick === undefined ? "-" : String(tick.stuck);
    this.elements.metricNodes.textContent = this.state.result === null
      ? "-"
      : `${this.state.result.navigationNodes} / ${this.state.result.rawNodes}`;
    this.elements.metricDuration.textContent = this.state.result === null
      ? "-"
      : `${Math.round(this.state.result.elapsedNanos / 1_000_000)} ms`;
    this.render();
  }

  private startGesture(event: PointerEvent): void {
    this.elements.canvas.setPointerCapture(event.pointerId);
    if (event.pointerType === "touch" && this.state.pointer !== null) {
      this.state.pinch = {
        distance: Math.hypot(event.offsetX - this.state.pointer.x, event.offsetY - this.state.pointer.y),
        zoom: this.state.zoom
      };
      return;
    }
    this.state.pointer = { id: event.pointerId, x: event.offsetX, y: event.offsetY };
  }

  private updateGesture(event: PointerEvent): void {
    if (this.state.pinch !== null && event.pointerType === "touch") {
      this.updatePinch(event);
      return;
    }
    if (this.state.pointer === null || this.state.pointer.id !== event.pointerId) {
      return;
    }
    const dx = event.offsetX - this.state.pointer.x;
    const dy = event.offsetY - this.state.pointer.y;
    if (event.buttons === 4) {
      this.state.yaw = normalizeRadians(this.state.yaw + dx * ROTATION_RADIANS_PER_PIXEL);
    } else {
      this.state.panX += dx;
      this.state.panY += dy;
    }
    this.state.pointer = { id: event.pointerId, x: event.offsetX, y: event.offsetY };
    this.render();
  }

  private endGesture(event: PointerEvent): void {
    if (this.state.pointer?.id === event.pointerId) {
      this.state.pointer = null;
    }
    this.state.pinch = null;
  }

  private updatePinch(event: PointerEvent): void {
    const anchor = this.state.pointer;
    if (anchor === null || this.state.pinch === null) {
      return;
    }
    const distance = Math.hypot(event.offsetX - anchor.x, event.offsetY - anchor.y);
    if (this.state.pinch.distance > 0) {
      this.state.zoom = clamp(this.state.pinch.zoom * distance / this.state.pinch.distance, 0.35, 6);
    }
    this.render();
  }

  private zoomWithWheel(event: WheelEvent): void {
    event.preventDefault();
    const rect = this.elements.canvas.getBoundingClientRect();
    const point = { x: event.clientX - rect.left, y: event.clientY - rect.top };
    const oldZoom = this.state.zoom;
    const factor = Math.pow(WHEEL_ZOOM_FACTOR, -Math.sign(event.deltaY));
    this.state.zoom = clamp(oldZoom * factor, 0.35, 6);
    const ratio = this.state.zoom / oldZoom;
    this.state.panX = point.x - (point.x - this.state.panX) * ratio;
    this.state.panY = point.y - (point.y - this.state.panY) * ratio;
    this.render();
  }

  private allowDrop(event: DragEvent): void {
    event.preventDefault();
    this.elements.dropZone.classList.add("drag-over");
  }

  private leaveDrop(): void {
    this.elements.dropZone.classList.remove("drag-over");
  }

  private async dropFile(event: DragEvent): Promise<void> {
    event.preventDefault();
    this.elements.dropZone.classList.remove("drag-over");
    const file = event.dataTransfer?.files[0];
    if (file !== undefined) {
      await this.loadReplayFile(file);
    }
  }

  private handleKeyboard(event: KeyboardEvent): void {
    if (event.target instanceof HTMLInputElement) {
      return;
    }
    if (event.key === " ") {
      event.preventDefault();
      this.togglePlayback();
    } else if (event.key === "ArrowLeft") {
      this.setFrame(this.state.frame - 1);
    } else if (event.key === "ArrowRight") {
      this.setFrame(this.state.frame + 1);
    } else if (event.key.toLowerCase() === "v") {
      this.resetView();
    } else if (event.key.toLowerCase() === "m") {
      this.toggleMode();
    }
  }

  private stopTimer(): void {
    if (this.state.timer !== null) {
      window.clearInterval(this.state.timer);
      this.state.timer = null;
    }
  }

  private render(): void {
    this.renderer.render(this.state);
  }
}

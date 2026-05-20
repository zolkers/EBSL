import type { ViewerElements } from "./dom.js";
import { clamp, normalizeRadians } from "./math-utils.js";
import { loadCatalogReplay, loadReplayCatalog, ReplayCatalogEntry } from "./replay-catalog.js";
import { parseReplay } from "./replay-model.js";
import { ReplayRenderer } from "./replay-renderer.js";
import {
  DirectoryEntry,
  DirectoryRoot,
  loadDirectories,
  loadDirectoryRoots,
  loadSimGoals,
  MinecraftRouteRequest,
  runMinecraftRoute,
  SimGoalDescriptor
} from "./sim-api.js";
import { createViewerState, maxFrame, resetCamera, TICK_MS, ViewerState } from "./viewer-state.js";

const WHEEL_ZOOM_FACTOR = 1.12;
const ROTATION_RADIANS_PER_PIXEL = 0.008;

export class ReplayController {
  private readonly state: ViewerState = createViewerState();
  private readonly renderer: ReplayRenderer;
  private catalog: readonly ReplayCatalogEntry[] = [];
  private goals: readonly SimGoalDescriptor[] = [];
  private browserPath = "";

  constructor(private readonly elements: ViewerElements) {
    this.renderer = new ReplayRenderer(elements.canvas);
  }

  start(): void {
    this.elements.fileInput.addEventListener("change", () => void this.loadSelectedFile());
    this.elements.openWorldButton.addEventListener("click", () => void this.openWorldBrowser());
    this.elements.savedReplaySelect.addEventListener("change", () => void this.loadSelectedCatalogReplay());
    this.elements.goalSelect.addEventListener("change", () => this.updateGoalSelection());
    this.elements.routeForm.addEventListener("submit", event => void this.runRoute(event));
    this.elements.browseWorldButton.addEventListener("click", () => void this.openWorldBrowser());
    this.elements.browserCloseButton.addEventListener("click", () => this.closeWorldBrowser());
    this.elements.selectWorldButton.addEventListener("click", () => this.applyWorldPath(this.browserPath));
    this.elements.worldBrowser.addEventListener("click", event => this.closeBrowserBackdrop(event));
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
    void this.loadCatalog();
    void this.loadGoals();
    this.render();
  }

  private async loadSelectedFile(): Promise<void> {
    const file = this.elements.fileInput.files?.[0];
    if (file !== undefined) {
      await this.loadReplayFile(file);
    }
  }

  private async loadReplayFile(file: File): Promise<void> {
    this.loadReplayPayload(await file.text());
  }

  private loadReplayPayload(payload: string): void {
    this.state.result = parseReplay(JSON.parse(payload));
    this.state.frame = 0;
    this.elements.timeline.max = maxFrame(this.state).toString();
    this.elements.timeline.value = "0";
    resetCamera(this.state);
    this.updateUi();
  }

  private async loadCatalog(): Promise<void> {
    this.catalog = await loadReplayCatalog();
    this.elements.savedReplaySelect.replaceChildren(this.catalogPlaceholder());
    for (const entry of this.catalog) {
      this.elements.savedReplaySelect.append(this.catalogOption(entry));
    }
  }

  private async loadSelectedCatalogReplay(): Promise<void> {
    const selected = this.catalog.find(entry => entry.file === this.elements.savedReplaySelect.value);
    if (selected !== undefined) {
      this.loadReplayPayload(await loadCatalogReplay(selected));
    }
  }

  private async loadGoals(): Promise<void> {
    this.goals = await loadSimGoals();
    this.elements.goalSelect.replaceChildren();
    this.elements.goalList.replaceChildren();
    for (const goal of this.goals) {
      const option = document.createElement("option");
      option.value = goal.id;
      option.textContent = goal.label;
      option.title = goal.description;
      this.elements.goalSelect.append(option);
    }
    if (this.goals.length === 0) {
      const option = document.createElement("option");
      option.value = "walk";
      option.textContent = "Walk";
      this.elements.goalSelect.append(option);
      this.elements.goalList.append(this.goalButton("walk", "Walk", "Fallback goal while the Java API is offline."));
      this.elements.serverStatus.textContent = "offline";
    } else {
      this.renderGoalList();
      this.elements.serverStatus.textContent = `${this.goals.length} goals`;
    }
    this.updateGoalSelection();
  }

  private renderGoalList(): void {
    this.elements.goalList.replaceChildren();
    for (const goal of this.goals) {
      this.elements.goalList.append(this.goalButton(goal.id, goal.label, goal.description));
    }
  }

  private goalButton(id: string, label: string, description: string): HTMLButtonElement {
    const button = document.createElement("button");
    button.type = "button";
    button.dataset.goal = id;
    button.textContent = label;
    button.title = description;
    button.addEventListener("click", () => {
      this.elements.goalSelect.value = id;
      this.updateGoalSelection();
    });
    return button;
  }

  private updateGoalSelection(): void {
    this.elements.goalList.querySelectorAll("button").forEach(button => {
      button.classList.toggle("selected", button.dataset.goal === this.elements.goalSelect.value);
    });
    this.renderGoalFields();
  }

  private renderGoalFields(): void {
    this.elements.goalFields.replaceChildren();
    const goal = this.selectedGoal();
    for (const parameter of goal?.parameters ?? this.walkParameters()) {
      const label = document.createElement("label");
      label.textContent = parameter.label;
      const input = document.createElement("input");
      input.type = "number";
      input.step = "1";
      input.value = parameter.defaultValue.toString();
      input.dataset.parameter = parameter.id;
      label.append(input);
      this.elements.goalFields.append(label);
    }
  }

  private async openWorldBrowser(): Promise<void> {
    this.elements.worldBrowser.classList.remove("hidden");
    this.elements.serverStatus.textContent = "browsing";
    const roots = await loadDirectoryRoots();
    this.renderBrowserRoots(roots);
    const current = this.elements.worldInput.value.trim();
    const fallback = roots[0]?.path ?? "";
    if (current !== "" || fallback !== "") {
      await this.loadBrowserPath(current || fallback);
    }
  }

  private closeWorldBrowser(): void {
    this.elements.worldBrowser.classList.add("hidden");
    this.elements.serverStatus.textContent = this.goals.length === 0 ? "offline" : "ready";
  }

  private closeBrowserBackdrop(event: MouseEvent): void {
    if (event.target === this.elements.worldBrowser) {
      this.closeWorldBrowser();
    }
  }

  private renderBrowserRoots(roots: readonly DirectoryRoot[]): void {
    this.elements.browserRoots.replaceChildren();
    for (const root of roots) {
      const button = document.createElement("button");
      button.type = "button";
      button.textContent = root.name;
      button.title = root.path;
      button.addEventListener("click", () => void this.loadBrowserPath(root.path));
      this.elements.browserRoots.append(button);
    }
  }

  private async loadBrowserPath(path: string): Promise<void> {
    try {
      const listing = await loadDirectories(path);
      this.browserPath = listing.path;
      this.elements.browserCurrent.textContent = listing.path;
      this.renderBrowserEntries(listing.parent, listing.entries);
      this.elements.serverStatus.textContent = "browse ready";
    } catch (error) {
      this.elements.serverStatus.textContent = error instanceof Error ? error.message : "browse failed";
    }
  }

  private renderBrowserEntries(parent: string | null, entries: readonly DirectoryEntry[]): void {
    this.elements.browserList.replaceChildren();
    if (parent !== null) {
      this.elements.browserList.append(this.browserRow("..", parent, false));
    }
    for (const entry of entries) {
      this.elements.browserList.append(this.browserRow(entry.name, entry.path, entry.world));
    }
  }

  private browserRow(name: string, path: string, world: boolean): HTMLElement {
    const row = document.createElement("div");
    row.className = world ? "browser-row world" : "browser-row";
    const openButton = document.createElement("button");
    openButton.type = "button";
    openButton.className = "browser-open";
    openButton.textContent = world ? `${name} world` : name;
    openButton.title = path;
    openButton.addEventListener("click", () => void this.loadBrowserPath(path));
    const chooseButton = document.createElement("button");
    chooseButton.type = "button";
    chooseButton.className = "browser-choose";
    chooseButton.textContent = "Use";
    chooseButton.title = path;
    chooseButton.addEventListener("click", () => this.applyWorldPath(path));
    row.append(openButton, chooseButton);
    return row;
  }

  private applyWorldPath(path: string): void {
    if (path === "") {
      return;
    }
    this.elements.worldInput.value = path;
    this.closeWorldBrowser();
  }

  private async runRoute(event: Event): Promise<void> {
    event.preventDefault();
    if (this.elements.worldInput.value.trim() === "") {
      this.elements.serverStatus.textContent = "world missing";
      return;
    }
    this.elements.serverStatus.textContent = "running";
    this.elements.runRouteButton.disabled = true;
    try {
      const payload = await runMinecraftRoute(this.routeRequest());
      this.loadReplayPayload(payload);
      await this.loadCatalog();
      this.elements.serverStatus.textContent = "done";
    } catch (error) {
      this.elements.serverStatus.textContent = error instanceof Error ? error.message : "failed";
    } finally {
      this.elements.runRouteButton.disabled = false;
    }
  }

  private routeRequest(): MinecraftRouteRequest {
    return {
      worldDirectory: this.elements.worldInput.value.trim(),
      startX: numberInput(this.elements.startXInput, 0.5),
      startY: numberInput(this.elements.startYInput, 64.0),
      startZ: numberInput(this.elements.startZInput, 0.5),
      goalId: this.elements.goalSelect.value || "walk",
      goalValues: this.goalValues(),
      maxTicks: integerInput(this.elements.maxTicksInput, 600),
      radiusChunks: integerInput(this.elements.radiusInput, 4),
      goalSearchBlocks: integerInput(this.elements.goalSearchInput, 96),
      saveReplay: this.elements.saveReplayInput.checked
    };
  }

  private goalValues(): Readonly<Record<string, number>> {
    const values: Record<string, number> = {};
    this.elements.goalFields.querySelectorAll("input").forEach(input => {
      const parameter = input.dataset.parameter;
      if (parameter !== undefined) {
        values[parameter] = signedIntegerInput(input, 0);
      }
    });
    return values;
  }

  private selectedGoal(): SimGoalDescriptor | undefined {
    return this.goals.find(goal => goal.id === this.elements.goalSelect.value);
  }

  private walkParameters(): readonly { readonly id: string; readonly label: string; readonly defaultValue: number }[] {
    return [
      { id: "x", label: "X", defaultValue: 500 },
      { id: "y", label: "Y", defaultValue: 61 },
      { id: "z", label: "Z", defaultValue: 40 }
    ];
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
    if (!this.elements.worldBrowser.classList.contains("hidden") && event.key === "Escape") {
      this.closeWorldBrowser();
      return;
    }
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

  private catalogPlaceholder(): HTMLOptionElement {
    const option = document.createElement("option");
    option.value = "";
    option.textContent = this.catalog.length === 0 ? "No saved replay" : "Saved replays";
    return option;
  }

  private catalogOption(entry: ReplayCatalogEntry): HTMLOptionElement {
    const option = document.createElement("option");
    option.value = entry.file;
    option.textContent = `${entry.scenarioId} · ${entry.status} · ${entry.ticks} ticks`;
    option.title = new Date(entry.savedAt).toLocaleString();
    return option;
  }
}

function numberInput(input: HTMLInputElement, fallback: number): number {
  const value = Number(input.value);
  return Number.isFinite(value) ? value : fallback;
}

function integerInput(input: HTMLInputElement, fallback: number): number {
  const value = Math.trunc(numberInput(input, fallback));
  return value > 0 ? value : fallback;
}

function signedIntegerInput(input: HTMLInputElement, fallback: number): number {
  return Math.trunc(numberInput(input, fallback));
}

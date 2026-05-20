export interface ViewerElements {
  readonly canvas: HTMLCanvasElement;
  readonly dropZone: HTMLElement;
  readonly fileInput: HTMLInputElement;
  readonly timeline: HTMLInputElement;
  readonly playButton: HTMLButtonElement;
  readonly resetViewButton: HTMLButtonElement;
  readonly modeButton: HTMLButtonElement;
  readonly savedReplaySelect: HTMLSelectElement;
  readonly emptyState: HTMLElement;
  readonly scenarioLabel: HTMLElement;
  readonly frameLabel: HTMLElement;
  readonly metricStatus: HTMLElement;
  readonly metricMove: HTMLElement;
  readonly metricDistance: HTMLElement;
  readonly metricStuck: HTMLElement;
  readonly metricNodes: HTMLElement;
  readonly metricDuration: HTMLElement;
}

export function queryViewerElements(): ViewerElements {
  return {
    canvas: query("#replay-canvas", HTMLCanvasElement),
    dropZone: query("#drop-zone", HTMLElement),
    fileInput: query("#file-input", HTMLInputElement),
    timeline: query("#timeline", HTMLInputElement),
    playButton: query("#play-button", HTMLButtonElement),
    resetViewButton: query("#reset-view-button", HTMLButtonElement),
    modeButton: query("#mode-button", HTMLButtonElement),
    savedReplaySelect: query("#saved-replay-select", HTMLSelectElement),
    emptyState: query("#empty-state", HTMLElement),
    scenarioLabel: query("#scenario-label", HTMLElement),
    frameLabel: query("#frame-label", HTMLElement),
    metricStatus: query("#metric-status", HTMLElement),
    metricMove: query("#metric-move", HTMLElement),
    metricDistance: query("#metric-distance", HTMLElement),
    metricStuck: query("#metric-stuck", HTMLElement),
    metricNodes: query("#metric-nodes", HTMLElement),
    metricDuration: query("#metric-duration", HTMLElement)
  };
}

function query<T extends Element>(selector: string, constructor: { new(): T }): T {
  const element = document.querySelector(selector);
  if (!(element instanceof constructor)) {
    throw new Error(`Missing required viewer element: ${selector}`);
  }
  return element;
}

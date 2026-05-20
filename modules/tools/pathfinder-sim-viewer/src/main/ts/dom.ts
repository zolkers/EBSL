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
  readonly serverStatus: HTMLElement;
  readonly routeForm: HTMLFormElement;
  readonly worldInput: HTMLInputElement;
  readonly browseWorldButton: HTMLButtonElement;
  readonly worldBrowser: HTMLElement;
  readonly browserCloseButton: HTMLButtonElement;
  readonly browserCurrent: HTMLElement;
  readonly browserRoots: HTMLElement;
  readonly browserList: HTMLElement;
  readonly selectWorldButton: HTMLButtonElement;
  readonly startXInput: HTMLInputElement;
  readonly startYInput: HTMLInputElement;
  readonly startZInput: HTMLInputElement;
  readonly goalSelect: HTMLSelectElement;
  readonly goalList: HTMLElement;
  readonly goalFields: HTMLElement;
  readonly maxTicksInput: HTMLInputElement;
  readonly radiusInput: HTMLInputElement;
  readonly goalSearchInput: HTMLInputElement;
  readonly saveReplayInput: HTMLInputElement;
  readonly runRouteButton: HTMLButtonElement;
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
    metricDuration: query("#metric-duration", HTMLElement),
    serverStatus: query("#server-status", HTMLElement),
    routeForm: query("#route-form", HTMLFormElement),
    worldInput: query("#world-input", HTMLInputElement),
    browseWorldButton: query("#browse-world-button", HTMLButtonElement),
    worldBrowser: query("#world-browser", HTMLElement),
    browserCloseButton: query("#browser-close-button", HTMLButtonElement),
    browserCurrent: query("#browser-current", HTMLElement),
    browserRoots: query("#browser-roots", HTMLElement),
    browserList: query("#browser-list", HTMLElement),
    selectWorldButton: query("#select-world-button", HTMLButtonElement),
    startXInput: query("#start-x-input", HTMLInputElement),
    startYInput: query("#start-y-input", HTMLInputElement),
    startZInput: query("#start-z-input", HTMLInputElement),
    goalSelect: query("#goal-select", HTMLSelectElement),
    goalList: query("#goal-list", HTMLElement),
    goalFields: query("#goal-fields", HTMLElement),
    maxTicksInput: query("#max-ticks-input", HTMLInputElement),
    radiusInput: query("#radius-input", HTMLInputElement),
    goalSearchInput: query("#goal-search-input", HTMLInputElement),
    saveReplayInput: query("#save-replay-input", HTMLInputElement),
    runRouteButton: query("#run-route-button", HTMLButtonElement)
  };
}

function query<T extends Element>(selector: string, constructor: { new(): T }): T {
  const element = document.querySelector(selector);
  if (!(element instanceof constructor)) {
    throw new Error(`Missing required viewer element: ${selector}`);
  }
  return element;
}

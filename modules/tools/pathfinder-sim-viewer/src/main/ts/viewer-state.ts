import type { ReplayResult } from "./replay-model.js";

export const TICK_MS = 50;
export const DEFAULT_YAW = Math.PI / 4;

export interface ViewerState {
  result: ReplayResult | null;
  frame: number;
  playing: boolean;
  mode: ViewMode;
  yaw: number;
  zoom: number;
  panX: number;
  panY: number;
  pointer: PointerSnapshot | null;
  pinch: PinchSnapshot | null;
  timer: number | null;
}

export interface PointerSnapshot {
  readonly id: number;
  readonly x: number;
  readonly y: number;
}

export interface PinchSnapshot {
  readonly distance: number;
  readonly zoom: number;
}

export type ViewMode = "2d" | "3d";

export function createViewerState(): ViewerState {
  return {
    result: null,
    frame: 0,
    playing: false,
    mode: "3d",
    yaw: DEFAULT_YAW,
    zoom: 1,
    panX: 0,
    panY: 0,
    pointer: null,
    pinch: null,
    timer: null
  };
}

export function resetCamera(state: ViewerState): void {
  state.zoom = 1;
  state.panX = 0;
  state.panY = 0;
  state.yaw = DEFAULT_YAW;
}

export function maxFrame(state: ViewerState): number {
  return state.result === null ? 0 : Math.max(0, state.result.trace.length - 1);
}

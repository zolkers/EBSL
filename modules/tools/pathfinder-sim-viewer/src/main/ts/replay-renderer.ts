import type { ReplayBlock, Vector3 } from "./replay-model.js";
import type { ViewerState } from "./viewer-state.js";
import { rgbToCss, shadeRgb } from "./math-utils.js";
import {
  Bounds,
  computeBounds,
  isoDepth,
  isoPoint,
  minTerrainY,
  Point2,
  screenPoint,
  screenX,
  screenY,
  surfaceTerrain
} from "./replay-geometry.js";

const COLORS = {
  background: "#12171d",
  grid: "#303946",
  path: "#50aaff",
  stuck: "#ff6060",
  player: "#73e691",
  text: "#e2e8f0"
} as const;

export class ReplayRenderer {
  private readonly context: CanvasRenderingContext2D;

  constructor(private readonly canvas: HTMLCanvasElement) {
    const context = canvas.getContext("2d");
    if (context === null) {
      throw new Error("Canvas 2D rendering is not available.");
    }
    this.context = context;
  }

  render(state: ViewerState): void {
    const rect = this.resizeCanvas();
    this.clear(rect);
    this.drawGrid(rect);
    if (state.result === null) {
      return;
    }
    const bounds = computeBounds(state.result);
    if (state.mode === "3d") {
      this.drawIsometric(state, rect, bounds);
    } else {
      this.drawTopDown(state, rect, bounds);
    }
  }

  private resizeCanvas(): DOMRect {
    const rect = this.canvas.getBoundingClientRect();
    const scale = window.devicePixelRatio || 1;
    const width = Math.max(1, Math.floor(rect.width * scale));
    const height = Math.max(1, Math.floor(rect.height * scale));
    if (this.canvas.width !== width || this.canvas.height !== height) {
      this.canvas.width = width;
      this.canvas.height = height;
      this.context.setTransform(scale, 0, 0, scale, 0, 0);
    }
    return rect;
  }

  private clear(rect: DOMRect): void {
    this.context.fillStyle = COLORS.background;
    this.context.fillRect(0, 0, rect.width, rect.height);
  }

  private drawGrid(rect: DOMRect): void {
    this.context.strokeStyle = COLORS.grid;
    this.context.lineWidth = 1;
    for (let x = 0; x < rect.width; x += 32) {
      this.line(x, 0, x, rect.height);
    }
    for (let y = 0; y < rect.height; y += 32) {
      this.line(0, y, rect.width, y);
    }
  }

  private drawTopDown(state: ViewerState, rect: DOMRect, bounds: Bounds): void {
    const result = state.result;
    if (result === null) {
      return;
    }
    for (const block of surfaceTerrain(result.terrain)) {
      const left = screenX(bounds, state, rect, block.x);
      const right = screenX(bounds, state, rect, block.x + 1);
      const top = screenY(bounds, state, rect, block.z + 1);
      const bottom = screenY(bounds, state, rect, block.z);
      this.context.fillStyle = rgbToCss(block.rgb);
      this.context.fillRect(
        Math.floor(Math.min(left, right)),
        Math.floor(Math.min(top, bottom)),
        Math.ceil(Math.abs(right - left)) + 1,
        Math.ceil(Math.abs(bottom - top)) + 1
      );
    }
    this.drawPath(state, rect, bounds, false);
    this.drawPlayer(state, rect, bounds, false);
  }

  private drawIsometric(state: ViewerState, rect: DOMRect, bounds: Bounds): void {
    const result = state.result;
    if (result === null) {
      return;
    }
    const terrainMinY = minTerrainY(result.terrain);
    const blocks = [...result.terrain].sort((left, right) =>
      isoDepth(bounds, state, left) - isoDepth(bounds, state, right)
    );
    for (const block of blocks) {
      this.drawIsoBlock(state, rect, bounds, terrainMinY, block);
    }
    this.drawPath(state, rect, bounds, true);
    this.drawPlayer(state, rect, bounds, true);
  }

  private drawIsoBlock(
    state: ViewerState,
    rect: DOMRect,
    bounds: Bounds,
    terrainMinY: number,
    block: ReplayBlock
  ): void {
    this.fillPolygon(this.isoFace(state, rect, bounds, terrainMinY, block, "south"), shadeRgb(block.rgb, -28));
    this.fillPolygon(this.isoFace(state, rect, bounds, terrainMinY, block, "east"), shadeRgb(block.rgb, -50));
    this.fillPolygon(this.isoFace(state, rect, bounds, terrainMinY, block, "top"), rgbToCss(block.rgb));
  }

  private isoFace(
    state: ViewerState,
    rect: DOMRect,
    bounds: Bounds,
    terrainMinY: number,
    block: ReplayBlock,
    face: "top" | "south" | "east"
  ): readonly Point2[] {
    const point = (x: number, y: number, z: number): Point2 =>
      isoPoint(bounds, state, rect, terrainMinY, x, y, z);
    if (face === "top") {
      return [
        point(block.x, block.y + 1, block.z),
        point(block.x + 1, block.y + 1, block.z),
        point(block.x + 1, block.y + 1, block.z + 1),
        point(block.x, block.y + 1, block.z + 1)
      ];
    }
    if (face === "east") {
      return [
        point(block.x + 1, block.y + 1, block.z),
        point(block.x + 1, block.y + 1, block.z + 1),
        point(block.x + 1, block.y, block.z + 1),
        point(block.x + 1, block.y, block.z)
      ];
    }
    return [
      point(block.x, block.y + 1, block.z + 1),
      point(block.x + 1, block.y + 1, block.z + 1),
      point(block.x + 1, block.y, block.z + 1),
      point(block.x, block.y, block.z + 1)
    ];
  }

  private drawPath(state: ViewerState, rect: DOMRect, bounds: Bounds, isometric: boolean): void {
    const result = state.result;
    if (result === null) {
      return;
    }
    this.context.lineWidth = 2;
    for (let i = 1; i <= state.frame; i += 1) {
      const previous = result.trace[i - 1];
      const current = result.trace[i];
      if (previous === undefined || current === undefined) {
        continue;
      }
      this.context.strokeStyle = current.stuck ? COLORS.stuck : COLORS.path;
      const start = this.project(state, rect, bounds, previous.position, isometric);
      const end = this.project(state, rect, bounds, current.position, isometric);
      this.line(start.x, start.y, end.x, end.y);
    }
  }

  private drawPlayer(state: ViewerState, rect: DOMRect, bounds: Bounds, isometric: boolean): void {
    const tick = state.result?.trace[state.frame];
    if (tick === undefined) {
      return;
    }
    const point = this.project(state, rect, bounds, tick.position, isometric);
    this.context.fillStyle = tick.stuck ? COLORS.stuck : COLORS.player;
    this.context.beginPath();
    this.context.arc(point.x, point.y - (isometric ? 10 : 0), isometric ? 7 : 6, 0, Math.PI * 2);
    this.context.fill();
    this.context.strokeStyle = COLORS.text;
    this.context.stroke();
  }

  private project(state: ViewerState, rect: DOMRect, bounds: Bounds, position: Vector3, isometric: boolean): Point2 {
    if (isometric && state.result !== null) {
      return isoPoint(bounds, state, rect, minTerrainY(state.result.terrain), position[0], position[1], position[2]);
    }
    return screenPoint(bounds, state, rect, position);
  }

  private fillPolygon(points: readonly Point2[], color: string): void {
    const first = points[0];
    if (first === undefined) {
      return;
    }
    this.context.beginPath();
    this.context.moveTo(first.x, first.y);
    for (const point of points.slice(1)) {
      this.context.lineTo(point.x, point.y);
    }
    this.context.closePath();
    this.context.fillStyle = color;
    this.context.fill();
    this.context.strokeStyle = "rgba(0, 0, 0, 0.18)";
    this.context.stroke();
  }

  private line(x1: number, y1: number, x2: number, y2: number): void {
    this.context.beginPath();
    this.context.moveTo(x1, y1);
    this.context.lineTo(x2, y2);
    this.context.stroke();
  }
}

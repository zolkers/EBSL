export function clamp(value: number, min: number, max: number): number {
  return Math.max(min, Math.min(value, max));
}

export function normalizeRadians(radians: number): number {
  const full = Math.PI * 2;
  const normalized = radians % full;
  return normalized < 0 ? normalized + full : normalized;
}

export function rgbToCss(rgb: number | undefined): string {
  const safeRgb = rgb ?? 0x6B7280;
  const red = (safeRgb >> 16) & 255;
  const green = (safeRgb >> 8) & 255;
  const blue = safeRgb & 255;
  return `rgb(${red}, ${green}, ${blue})`;
}

export function shadeRgb(rgb: number | undefined, delta: number): string {
  const safeRgb = rgb ?? 0x6B7280;
  const red = clamp(((safeRgb >> 16) & 255) + delta, 0, 255);
  const green = clamp(((safeRgb >> 8) & 255) + delta, 0, 255);
  const blue = clamp((safeRgb & 255) + delta, 0, 255);
  return `rgb(${red}, ${green}, ${blue})`;
}

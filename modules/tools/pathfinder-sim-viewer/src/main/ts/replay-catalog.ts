export interface ReplayCatalogEntry {
  readonly savedAt: string;
  readonly file: string;
  readonly scenarioId: string;
  readonly status: string;
  readonly reached: boolean;
  readonly ticks: number;
  readonly resultCount: number;
}

interface ReplayCatalogFile {
  readonly replays?: readonly ReplayCatalogEntry[];
}

export async function loadReplayCatalog(): Promise<readonly ReplayCatalogEntry[]> {
  const response = await fetchFirst(["api/replays", "replays/index.json"]);
  if (!response.ok) {
    return [];
  }
  const catalog = await response.json() as ReplayCatalogFile;
  return Array.isArray(catalog.replays) ? catalog.replays : [];
}

export async function loadCatalogReplay(entry: ReplayCatalogEntry): Promise<string> {
  const encodedFile = encodeURIComponent(entry.file);
  const response = await fetchFirst([`api/replays/${encodedFile}`, `replays/${encodedFile}`]);
  if (!response.ok) {
    throw new Error(`Unable to load saved replay: ${entry.file}`);
  }
  return response.text();
}

async function fetchFirst(urls: readonly string[]): Promise<Response> {
  let fallback = new Response(null, { status: 404 });
  for (const url of urls) {
    try {
      const response = await fetch(url, { cache: "no-store" });
      if (response.ok) {
        return response;
      }
      fallback = response;
    } catch {
      fallback = new Response(null, { status: 503 });
    }
  }
  return fallback;
}

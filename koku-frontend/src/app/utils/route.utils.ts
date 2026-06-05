export interface RouteBasedFieldOverride {
  '@type': string;
  alias?: string;
  disable?: boolean;
  routeParam?: string;
}

export interface ResolvedFieldOverride {
  alias: string;
  disable: boolean;
  value: string;
}

export const replaceRouteSegments = (
  rawValue: string | undefined,
  routeSegments: Record<string, string> | null | undefined,
) => {
  let result = rawValue || '';
  for (const [segment, value] of Object.entries(routeSegments || {})) {
    result = result.replace(segment, value);
  }
  return result;
};

export const resolveRoutePath = (
  route: string | undefined,
  routeSegments: Record<string, string> | null | undefined,
) => {
  if (!route) {
    return undefined;
  }
  return route
    .split('/')
    .map((part) => (part.startsWith(':') ? routeSegments?.[part] : part))
    .join('/');
};

export const resolveRouteBasedFieldOverrides = <TOverride extends RouteBasedFieldOverride>(
  overrides: readonly TOverride[] | undefined,
  routeSegments: Record<string, string> | null | undefined,
): ResolvedFieldOverride[] => {
  const result: ResolvedFieldOverride[] = [];
  for (const override of overrides || []) {
    if (override['@type'] !== 'route-based-override' || !override.alias) {
      continue;
    }
    const value = routeSegments?.[override.routeParam || ''];
    if (value !== undefined) {
      result.push({
        alias: override.alias,
        disable: override.disable === true,
        value,
      });
    }
  }
  return result;
};

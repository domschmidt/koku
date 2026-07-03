export interface RouteBasedContentOverride {
  '@type': string;
  alias?: string;
  disabled?: boolean;
  routeParam?: string;
}

export interface ResolvedContentOverride {
  alias: string;
  disabled: boolean;
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

export const routePathSegments = (route: string | undefined): string[] =>
  (route || '').split('/').filter((value) => value !== '');

export const childRouteSegments = (url: string, parentRoutePath: string): string[] => {
  return routePathSegments(url.split('?')[0]).slice(routePathSegments(parentRoutePath).length);
};

export const matchRouteSegments = (
  route: string | undefined,
  segments: readonly string[],
): Record<string, string> | null => {
  if (!route) {
    return null;
  }

  const segmentMapping: Record<string, string> = {};
  const routeSegments = routePathSegments(route);
  for (let segmentIdx = 0; segmentIdx < routeSegments.length; segmentIdx++) {
    const routeSegment = routeSegments[segmentIdx];
    const currentSegment = segments[segmentIdx];
    if (!currentSegment) {
      return null;
    }
    if (routeSegment.startsWith(':')) {
      segmentMapping[routeSegment] ??= currentSegment;
    } else if (routeSegment !== currentSegment) {
      return null;
    }
  }
  return segmentMapping;
};

export const resolvedRoutePath = (
  parentRoutePath: string,
  route: string | undefined,
  routeSegments: Record<string, string> | null | undefined,
): string => {
  return `${parentRoutePath}/${route || ''}`
    .split('/')
    .map((value) => routeSegments?.[value] || value)
    .filter((value) => value !== '')
    .join('/');
};

export const resolvedRouteParts = (
  route: string | undefined,
  routeSegments: Record<string, string> | null | undefined,
): string[] => routePathSegments(route).map((part) => replaceRouteSegments(part, routeSegments));

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

export const resolveRouteBasedContentOverrides = <TOverride extends RouteBasedContentOverride>(
  overrides: readonly TOverride[] | undefined,
  routeSegments: Record<string, string> | null | undefined,
): ResolvedContentOverride[] => {
  const result: ResolvedContentOverride[] = [];
  for (const override of overrides || []) {
    if (override['@type'] !== 'route-based-override' || !override.alias) {
      continue;
    }
    const value = routeSegments?.[override.routeParam || ''];
    if (value !== undefined) {
      result.push({
        alias: override.alias,
        disabled: override.disabled === true,
        value,
      });
    }
  }
  return result;
};

import {
  childRouteSegments,
  matchRouteSegments,
  resolvedRouteParts,
  resolvedRoutePath,
  resolveRouteBasedContentOverrides,
  resolveRoutePath,
  routePathSegments,
} from './route.utils';

describe('resolveRouteBasedContentOverrides', () => {
  it('resolves route values while preserving the content alias', () => {
    expect(
      resolveRouteBasedContentOverrides(
        [
          {
            '@type': 'route-based-override',
            alias: 'customerId',
            disabled: true,
            routeParam: ':customerId',
          },
        ],
        { ':customerId': '41', ':appointmentId': '1449' },
      ),
    ).toEqual([{ alias: 'customerId', disabled: true, value: '41' }]);
  });

  it('ignores unresolved or incomplete overrides', () => {
    expect(
      resolveRouteBasedContentOverrides(
        [
          { '@type': 'route-based-override', alias: 'customerId', routeParam: ':customerId' },
          { '@type': 'route-based-override', routeParam: ':appointmentId' },
        ],
        { ':appointmentId': '1449' },
      ),
    ).toEqual([]);
  });
});

describe('route utilities', () => {
  it('normalizes route path segments', () => {
    expect(routePathSegments('/manage/customers/')).toEqual(['manage', 'customers']);
  });

  it('extracts child route segments without query parameters', () => {
    expect(childRouteSegments('/manage/customers/42/information?tab=details', '/manage/customers')).toEqual([
      '42',
      'information',
    ]);
  });

  it('matches parameterized route segments', () => {
    expect(matchRouteSegments(':customerId/information', ['42', 'information'])).toEqual({ ':customerId': '42' });
  });

  it('rejects non-matching route segments', () => {
    expect(matchRouteSegments(':customerId/appointments', ['42', 'information'])).toBeNull();
  });

  it('resolves parent route paths with route parameters', () => {
    expect(resolvedRoutePath('/manage/customers', ':customerId/information', { ':customerId': '42' })).toBe(
      'manage/customers/42/information',
    );
  });

  it('resolves navigable route parts with route parameters', () => {
    expect(resolvedRouteParts(':customerId/information', { ':customerId': '42' })).toEqual(['42', 'information']);
  });

  it('keeps an absent route absent', () => {
    expect(resolveRoutePath(undefined, { ':customerId': '42' })).toBeUndefined();
  });
});

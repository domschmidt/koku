import { resolveRouteBasedContentOverrides } from './route.utils';

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

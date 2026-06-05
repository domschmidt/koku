import { resolveRouteBasedFieldOverrides } from './route.utils';

describe('resolveRouteBasedFieldOverrides', () => {
  it('resolves route values while preserving the field alias', () => {
    expect(
      resolveRouteBasedFieldOverrides(
        [
          {
            '@type': 'route-based-override',
            alias: 'customerId',
            disable: true,
            routeParam: ':customerId',
          },
        ],
        { ':customerId': '41', ':appointmentId': '1449' },
      ),
    ).toEqual([{ alias: 'customerId', disable: true, value: '41' }]);
  });

  it('ignores unresolved or incomplete overrides', () => {
    expect(
      resolveRouteBasedFieldOverrides(
        [
          { '@type': 'route-based-override', alias: 'customerId', routeParam: ':customerId' },
          { '@type': 'route-based-override', routeParam: ':appointmentId' },
        ],
        { ':appointmentId': '1449' },
      ),
    ).toEqual([]);
  });
});

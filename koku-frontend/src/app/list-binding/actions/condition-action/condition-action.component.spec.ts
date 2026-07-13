import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';
import { ConditionActionComponent } from './condition-action.component';

describe('ConditionActionComponent', () => {
  it('selects positive and negative nested actions from item values', async () => {
    const positiveFactory = vi.fn((context) => ({ component: class {}, context }));
    const negativeFactory = vi.fn((context) => ({ component: class {}, context }));
    const parent = {
      contentSetup: signal({ actionRegistry: { positive: positiveFactory, negative: negativeFactory } }),
      listRegister: signal([]),
      urlSegments: signal(null),
    };
    const register = { source: signal({ status: 'ACTIVE' }) };
    const config = {
      compareValuePath: 'status',
      expectedValues: ['ACTIVE', 'PENDING'],
      positiveAction: { '@type': 'positive' },
      negativeAction: { '@type': 'negative' },
    };
    await TestBed.configureTestingModule({ imports: [ConditionActionComponent] })
      .overrideComponent(ConditionActionComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(ConditionActionComponent);
    fixture.componentRef.setInput('value', config);
    fixture.componentRef.setInput('register', register);
    fixture.componentRef.setInput('parent', parent);
    fixture.detectChanges();
    expect(fixture.componentInstance.matchesPositively(config as any, register as any)).toBe(true);
    expect(fixture.componentInstance.actionRecipe()).toEqual(
      expect.objectContaining({ component: expect.any(Function) }),
    );
    register.source.set({ status: 'INACTIVE' });
    expect(fixture.componentInstance.matchesPositively(config as any, register as any)).toBe(false);
    expect(fixture.componentInstance.actionRecipe()).toEqual(
      expect.objectContaining({ component: expect.any(Function) }),
    );
    expect(positiveFactory).toHaveBeenCalled();
    expect(negativeFactory).toHaveBeenCalled();
    expect(positiveFactory.mock.calls[0][0].action()).toBe(config.positiveAction);
    fixture.componentRef.setInput('value', { ...config });
    expect(fixture.componentInstance.actionRecipe()).toEqual(
      expect.objectContaining({ component: expect.any(Function) }),
    );
    fixture.componentRef.setInput('value', { compareValuePath: 'status', expectedValues: ['ACTIVE'] });
    expect(() => fixture.componentInstance.actionRecipe()).toThrow('no matching action');
    const identity = { action: config.positiveAction, parent, factory: positiveFactory };
    expect((fixture.componentInstance as any).sameActionIdentity(identity, identity)).toBe(true);
  });
});

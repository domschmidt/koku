import { TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';
import { FormularRuntime } from '../../../formular/formular.component';
import { ConditionalContainerComponent } from './conditional-container.component';

describe('ConditionalContainerComponent', () => {
  it('evaluates the configured source value against expected values', async () => {
    const runtime = new FormularRuntime(() => undefined);
    runtime.replaceSource({ customer: { status: 'ACTIVE' } });
    await TestBed.configureTestingModule({ imports: [ConditionalContainerComponent] })
      .overrideComponent(ConditionalContainerComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(ConditionalContainerComponent);
    fixture.componentRef.setInput('runtime', runtime);
    fixture.componentRef.setInput('contentRegistry', {});
    fixture.componentRef.setInput('content', {
      '@type': 'conditional-container',
      compareValuePath: 'customer.status',
      expectedValues: ['ACTIVE', 'PAUSED'],
    });
    fixture.detectChanges();
    expect(fixture.componentInstance.matchesPositively()).toBe(true);
    runtime.replaceSource({ customer: { status: 'DELETED' } });
    expect(fixture.componentInstance.matchesPositively()).toBe(false);
  });
});

import { TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';
import { FormularRuntime } from '../../../formular/formular.component';
import { FieldsetContainerComponent } from './fieldset-container.component';

describe('FieldsetContainerComponent', () => {
  it('binds formular runtime state', async () => {
    await TestBed.configureTestingModule({ imports: [FieldsetContainerComponent] })
      .overrideComponent(FieldsetContainerComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(FieldsetContainerComponent);
    fixture.componentRef.setInput('runtime', new FormularRuntime(() => undefined));
    fixture.componentRef.setInput('contentRegistry', {});
    fixture.componentRef.setInput('content', { '@type': 'fieldset' });
    fixture.componentRef.setInput('loading', 'true');
    fixture.componentRef.setInput('submitting', true);
    fixture.detectChanges();
    expect(fixture.componentInstance.loading()).toBe(true);
    expect(fixture.componentInstance.submitting()).toBe(true);
  });
});

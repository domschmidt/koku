import { TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';
import { FormularRuntime } from '../../../formular/formular.component';
import { GridContainerComponent } from './grid-container.component';

describe('GridContainerComponent', () => {
  it('binds formular runtime state', async () => {
    await TestBed.configureTestingModule({ imports: [GridContainerComponent] })
      .overrideComponent(GridContainerComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(GridContainerComponent);
    fixture.componentRef.setInput('runtime', new FormularRuntime(() => undefined));
    fixture.componentRef.setInput('contentRegistry', {});
    fixture.componentRef.setInput('content', { '@type': 'grid' });
    fixture.componentRef.setInput('loading', 'true');
    fixture.componentRef.setInput('submitting', true);
    fixture.detectChanges();
    expect(fixture.componentInstance.loading()).toBe(true);
    expect(fixture.componentInstance.submitting()).toBe(true);
  });
});

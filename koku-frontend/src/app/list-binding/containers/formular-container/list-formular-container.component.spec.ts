import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';
import { ListFormularContainerComponent } from './list-formular-container.component';

describe('ListFormularContainerComponent', () => {
  it('emits close and routes produced by save events', async () => {
    await TestBed.configureTestingModule({ imports: [ListFormularContainerComponent] })
      .overrideComponent(ListFormularContainerComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(ListFormularContainerComponent);
    fixture.componentRef.setInput('formularUrl', '/form');
    fixture.componentRef.setInput('contentRegistry', {});
    fixture.componentRef.setInput('onSaveEvents', [
      {
        '@type': 'open-routed-inline-formular',
        route: 'customers/:id',
        params: [{ '@type': 'event-payload', param: ':id', valuePath: 'id' }],
      },
    ]);
    fixture.detectChanges();
    const closed = vi.fn();
    const routed = vi.fn();
    fixture.componentInstance.closeRequested.subscribe(closed);
    fixture.componentInstance.openRoutedContentRequested.subscribe(routed);
    fixture.componentInstance.closeInlineContent();
    fixture.componentInstance.onFormularSave({ id: 42 });
    expect(closed).toHaveBeenCalled();
    expect(routed).toHaveBeenCalledWith(['customers', '42']);
  });
});

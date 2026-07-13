import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';
import { MultiSelectWithPricesFieldComponent } from './multi-select-with-prices-field.component';

describe('MultiSelectWithPricesFieldComponent', () => {
  it('edits prices and keeps ids and values synchronized', async () => {
    await TestBed.configureTestingModule({ imports: [MultiSelectWithPricesFieldComponent] })
      .overrideComponent(MultiSelectWithPricesFieldComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(MultiSelectWithPricesFieldComponent);
    fixture.componentRef.setInput('name', 'products');
    fixture.componentRef.setInput('idPathMapping', 'productId');
    fixture.componentRef.setInput('pricePathMapping', 'price');
    fixture.componentRef.setInput('possibleValues', [{ id: 'one' }, { id: 'two' }, { id: 'three' }]);
    fixture.componentRef.setInput('value', [
      { productId: 'one', price: 10 },
      { productId: 'two', price: null },
    ]);
    fixture.detectChanges();
    const changed = vi.fn();
    fixture.componentInstance.changed.subscribe(changed);
    fixture.componentInstance.selectItemClicked({ event: new Event('click'), id: 'one', pos: 0 });
    fixture.componentInstance.priceModal.update((modal) => ({ ...modal!, price: 12, useDefaultPrice: false }));
    fixture.componentInstance.applyPrice();
    expect(fixture.componentInstance.selectionValues()[0].price).toBe(12);
    fixture.componentInstance.selectItemClicked({ event: new Event('click'), id: 'two', pos: 1 });
    expect(fixture.componentInstance.priceModal()?.useDefaultPrice).toBe(true);
    fixture.componentInstance.applyPrice();
    fixture.componentInstance.handleMoveSelectionRequested({ oldPosition: 0, newPosition: 1, id: 'one' });
    fixture.componentInstance.handleDeleteSelectionRequested({ position: 0, id: 'two' });
    fixture.componentInstance.handleAddSelectionRequested('three');
    fixture.componentInstance.closePriceModal();
    expect(changed).toHaveBeenCalled();
    expect(fixture.componentInstance.selectionIds()).toEqual(['one', 'three']);
    expect(() =>
      fixture.componentInstance.selectItemClicked({ event: new Event('click'), id: 'none', pos: 99 }),
    ).toThrow('Selected item with pos 99 not found');
    fixture.componentRef.setInput('value', [{ productId: 7, price: 2 }]);
    fixture.detectChanges();
    expect(fixture.componentInstance.selectionIds()).toEqual(['7']);
    fixture.destroy();
  });
});

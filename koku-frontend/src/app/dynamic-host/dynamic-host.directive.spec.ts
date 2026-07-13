import { Component, computed, input, output, signal, viewChild } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { assertConcreteDynamicInputs, DynamicRenderRecipe, KokuDynamicHostDirective } from './dynamic-host.directive';

@Component({
  selector: 'koku-test-form-content',
  template: 'form: {{ formularUrl() }}',
})
class TestFormContentComponent {
  formularUrl = input.required<string>();
}

@Component({
  selector: 'koku-test-list-content',
  template: 'list: {{ listUrl() }}',
})
class TestListContentComponent {
  listUrl = input.required<string>();
  optional = input<string>();
  selected = output<string>();
}

@Component({
  imports: [KokuDynamicHostDirective],
  template:
    '<ng-container [kokuDynamicHost]="recipe()" (kokuDynamicCreated)="created($event)" (kokuDynamicLoadError)="loadError($event)" />',
})
class TestDynamicHostComponent {
  hostDirective = viewChild.required(KokuDynamicHostDirective);
  formularUrl = signal('/form');
  created = vi.fn();
  loadError = vi.fn();
  recipe = signal<DynamicRenderRecipe>({
    componentType: TestFormContentComponent,
    inputs: computed(() => ({ formularUrl: this.formularUrl() })),
  });
}

describe('assertConcreteDynamicInputs', () => {
  it('accepts resolved values and callbacks', () => {
    expect(() =>
      assertConcreteDynamicInputs({
        content: { id: 'content' },
        loading: false,
        close: () => undefined,
      }),
    ).not.toThrow();
  });

  it('rejects writable signals with the affected input name', () => {
    expect(() => assertConcreteDynamicInputs({ content: signal({ id: 'content' }) })).toThrowError(
      /Dynamic input 'content' received a Signal/,
    );
  });

  it('rejects computed signals before they reach the component', () => {
    const sourceUrl = computed(() => '/api/content');

    expect(() => assertConcreteDynamicInputs({ sourceUrl })).toThrowError(
      /Dynamic input 'sourceUrl' received a Signal/,
    );
  });

  it('switches component and inputs as one recipe', () => {
    const fixture = TestBed.createComponent(TestDynamicHostComponent);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('form: /form');

    fixture.componentInstance.formularUrl.set('/updated-form');
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('form: /updated-form');

    fixture.componentInstance.recipe.set({
      componentType: TestListContentComponent,
      inputs: computed(() => ({ listUrl: '/list', optional: 'present' })),
    });

    expect(() => fixture.detectChanges()).not.toThrow();
    expect(fixture.nativeElement.textContent).toContain('list: /list');
    expect(fixture.nativeElement.textContent).not.toContain('form:');
    fixture.componentInstance.recipe.set({
      componentType: TestListContentComponent,
      inputs: computed(() => ({ listUrl: '/list' })),
    });
    fixture.detectChanges();
  });

  it('loads components asynchronously, connects outputs and reports invalid recipes', async () => {
    const fixture = TestBed.createComponent(TestDynamicHostComponent);
    fixture.detectChanges();
    const selected = vi.fn();
    fixture.componentInstance.recipe.set({
      loadComponent: () => Promise.resolve(TestListContentComponent),
      inputs: computed(() => ({ listUrl: '/async-list' })),
      outputs: { selected },
    });
    fixture.detectChanges();
    await Promise.resolve();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('list: /async-list');
    const list = fixture.componentInstance.created.mock.calls.at(-1)?.[0].instance as TestListContentComponent;
    list.selected.emit('42');
    expect(selected).toHaveBeenCalledWith('42');
    expect(fixture.componentInstance.created).toHaveBeenCalled();

    fixture.componentInstance.recipe.set({
      componentType: TestListContentComponent,
      inputs: computed(() => ({ listUrl: '/without-output' })),
      outputs: { missing: vi.fn() },
    });
    fixture.detectChanges();
    expect(fixture.componentInstance.loadError).toHaveBeenCalledWith(expect.any(Error));

    fixture.componentInstance.recipe.set({
      loadComponent: () => Promise.reject(new Error('dynamic import failed')),
    });
    fixture.componentInstance.loadError.mockClear();
    fixture.detectChanges();
    await vi.waitFor(() =>
      expect(fixture.componentInstance.loadError).toHaveBeenCalledWith(
        expect.objectContaining({ message: 'dynamic import failed' }),
      ),
    );
    const directive = fixture.componentInstance.hostDirective();
    fixture.componentInstance.recipe.set(null as any);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).not.toContain('list:');
    expect(() => (directive as any).applyInputs({})).not.toThrow();
    expect(() => (directive as any).connectOutputs({})).not.toThrow();
    fixture.destroy();
  });
});

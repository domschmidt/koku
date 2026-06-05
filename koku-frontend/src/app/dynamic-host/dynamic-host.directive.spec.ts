import { Component, computed, input, signal } from '@angular/core';
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
}

@Component({
  imports: [KokuDynamicHostDirective],
  template: '<ng-container [kokuDynamicHost]="recipe()" />',
})
class TestDynamicHostComponent {
  formularUrl = signal('/form');
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
      inputs: computed(() => ({ listUrl: '/list' })),
    });

    expect(() => fixture.detectChanges()).not.toThrow();
    expect(fixture.nativeElement.textContent).toContain('list: /list');
    expect(fixture.nativeElement.textContent).not.toContain('form:');
  });
});

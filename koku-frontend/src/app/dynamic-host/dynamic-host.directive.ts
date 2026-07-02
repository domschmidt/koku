import {
  ComponentRef,
  Directive,
  OnDestroy,
  Signal,
  Type,
  ViewContainerRef,
  effect,
  input,
  isSignal,
  output,
  untracked,
  inject,
} from '@angular/core';

export type DynamicInputs = Readonly<Record<string, unknown>>;
export type DynamicOutputs = Record<string, (payload: any) => void>;

export function assertConcreteDynamicInputs(inputs: DynamicInputs | null | undefined): void {
  for (const [inputName, inputValue] of Object.entries(inputs ?? {})) {
    if (isSignal(inputValue)) {
      throw new Error(`Dynamic input '${inputName}' received a Signal. Read it inside the recipe inputs computation.`);
    }
  }
}

export interface DynamicRenderRecipe {
  componentType?: Type<unknown>;
  loadComponent?: () => Promise<Type<unknown>>;
  inputs?: Signal<DynamicInputs>;
  outputs?: DynamicOutputs;
}

interface DynamicOutputSubscription {
  unsubscribe(): void;
}

@Directive({
  selector: '[kokuDynamicHost]',
  standalone: true,
})
export class KokuDynamicHostDirective implements OnDestroy {
  kokuDynamicHost = input<DynamicRenderRecipe | null | undefined>(null);
  kokuDynamicProjectableNodes = input<Node[][]>([]);
  kokuDynamicCreated = output<ComponentRef<unknown>>();
  kokuDynamicLoadError = output<unknown>();

  readonly viewContainerRef = inject(ViewContainerRef);

  private componentRef: ComponentRef<unknown> | null = null;
  private currentComponentType: Type<unknown> | null = null;
  private outputSubscriptions: DynamicOutputSubscription[] = [];
  private readonly appliedInputs = new Map<string, unknown>();
  private connectedOutputs: DynamicOutputs | null | undefined;
  private activeRecipe: DynamicRenderRecipe | null | undefined;
  private activeInputs: DynamicInputs | null | undefined;
  private activeComponentSource: Type<unknown> | (() => Promise<Type<unknown>>) | null = null;
  private renderRequest = 0;

  constructor() {
    effect(() => {
      const recipe = this.kokuDynamicHost();
      const inputs = recipe?.inputs?.();
      untracked(() => this.applyRecipe(recipe, inputs));
    });
  }

  ngOnDestroy(): void {
    this.renderRequest++;
    this.destroyComponent();
  }

  private applyRecipe(recipe: DynamicRenderRecipe | null | undefined, inputs: DynamicInputs | null | undefined) {
    this.activeRecipe = recipe;
    this.activeInputs = inputs;
    const componentSource = recipe?.componentType ?? recipe?.loadComponent ?? null;
    if (componentSource !== this.activeComponentSource) {
      this.activeComponentSource = componentSource;
      this.resolveComponent(recipe?.componentType, recipe?.loadComponent);
      return;
    }
    this.applyInputs(inputs);
    this.connectOutputs(recipe?.outputs);
  }

  private resolveComponent(
    componentType: Type<unknown> | null | undefined,
    componentLoader: (() => Promise<Type<unknown>>) | null | undefined,
  ) {
    const request = ++this.renderRequest;
    if (componentType) {
      try {
        this.render(componentType);
      } catch (error) {
        this.destroyComponent();
        this.kokuDynamicLoadError.emit(error);
      }
      return;
    }
    if (!componentLoader) {
      this.render(null);
      return;
    }
    componentLoader()
      .then((loadedType) => {
        if (request === this.renderRequest) {
          this.render(loadedType);
        }
      })
      .catch((error) => {
        if (request === this.renderRequest) {
          this.destroyComponent();
          this.kokuDynamicLoadError.emit(error);
        }
      });
  }

  private render(componentType: Type<unknown> | null) {
    if (this.componentRef && this.currentComponentType === componentType) {
      this.applyInputs(this.activeInputs);
      this.connectOutputs(this.activeRecipe?.outputs);
      return;
    }

    this.destroyComponent();
    this.viewContainerRef.clear();
    if (!componentType) {
      return;
    }

    this.componentRef = this.viewContainerRef.createComponent(componentType, {
      projectableNodes: untracked(() => this.kokuDynamicProjectableNodes()),
    });
    this.currentComponentType = componentType;
    this.applyInputs(this.activeInputs);
    this.connectOutputs(this.activeRecipe?.outputs);
    this.kokuDynamicCreated.emit(this.componentRef);
  }

  private applyInputs(inputs: DynamicInputs | null | undefined) {
    if (!this.componentRef) {
      return;
    }
    assertConcreteDynamicInputs(inputs);
    const resolvedInputs = Object.entries(inputs ?? {});
    const inputNames = new Set(resolvedInputs.map(([inputName]) => inputName));
    for (const inputName of this.appliedInputs.keys()) {
      if (!inputNames.has(inputName)) {
        this.componentRef.setInput(inputName, undefined);
        this.appliedInputs.delete(inputName);
      }
    }
    for (const [inputName, inputValue] of resolvedInputs) {
      if (!this.appliedInputs.has(inputName) || !Object.is(this.appliedInputs.get(inputName), inputValue)) {
        this.componentRef.setInput(inputName, inputValue);
        this.appliedInputs.set(inputName, inputValue);
      }
    }
  }

  private connectOutputs(outputs: DynamicOutputs | null | undefined) {
    if (!this.componentRef) {
      return;
    }
    if (this.connectedOutputs === outputs) {
      return;
    }
    this.clearOutputSubscriptions();
    this.connectedOutputs = outputs;
    const instance = this.componentRef.instance as Record<string, any>;
    for (const [outputName, handler] of Object.entries(outputs ?? {})) {
      const outputRef = instance[outputName];
      if (!outputRef || typeof outputRef.subscribe !== 'function') {
        throw new Error(
          `Dynamic output '${outputName}' is not declared by ${this.currentComponentType?.name ?? 'component'}`,
        );
      }
      this.outputSubscriptions.push(outputRef.subscribe((payload: any) => handler(payload)));
    }
  }

  private destroyComponent() {
    this.clearOutputSubscriptions();
    this.componentRef?.destroy();
    this.componentRef = null;
    this.currentComponentType = null;
    this.appliedInputs.clear();
  }

  private clearOutputSubscriptions() {
    for (const subscription of this.outputSubscriptions) {
      subscription.unsubscribe();
    }
    this.outputSubscriptions = [];
    this.connectedOutputs = undefined;
  }
}

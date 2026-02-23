class UniqueRefGenerator {
  private currentCount = 0;

  public generate() {
    return `unique-ref-${++this.currentCount}`;
  }
}

export const UNIQUE_REF_GENERATOR = new UniqueRefGenerator();

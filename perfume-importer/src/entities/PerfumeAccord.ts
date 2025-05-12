import { Entity, ManyToOne, Property } from "@mikro-orm/core";
import { BaseEntity } from "./BaseEntity";
import { Perfume } from "./Perfume";
import { Accord } from "./Accord";

@Entity()
export class PerfumeAccord extends BaseEntity {
  @ManyToOne(() => Perfume)
  perfume!: Perfume;

  @ManyToOne(() => Accord)
  accord!: Accord;

  @Property()
  position?: number;

  constructor(perfume: Perfume, accord: Accord, position?: number) {
    super();
    this.perfume = perfume;
    this.accord = accord;
    this.position = position;
  }
}

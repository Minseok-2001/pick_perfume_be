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

  @Property({ nullable: true })
  strength?: number;

  constructor(perfume: Perfume, accord: Accord, strength?: number) {
    super();
    this.perfume = perfume;
    this.accord = accord;
    this.strength = strength;
  }
}

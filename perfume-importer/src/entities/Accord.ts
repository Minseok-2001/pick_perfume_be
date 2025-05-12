import { Entity, Property, OneToMany, Collection } from "@mikro-orm/core";
import { BaseEntity } from "./BaseEntity";
import { PerfumeAccord } from "./PerfumeAccord";

@Entity()
export class Accord extends BaseEntity {
  @Property({ unique: true })
  name!: string;

  @Property({ nullable: true })
  content?: string;

  @Property({ nullable: true })
  color?: string;

  @OneToMany(() => PerfumeAccord, (perfumeAccord) => perfumeAccord.accord)
  perfumeAccords = new Collection<PerfumeAccord>(this);

  constructor(name: string, content?: string, color?: string) {
    super();
    this.name = name;
    this.content = content;
    this.color = color;
  }
}

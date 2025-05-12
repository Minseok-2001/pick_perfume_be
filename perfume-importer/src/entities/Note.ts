import { Entity, Property, OneToMany, Collection } from "@mikro-orm/core";
import { BaseEntity } from "./BaseEntity";
import { PerfumeNote } from "./PerfumeNote";

@Entity()
export class Note extends BaseEntity {
  @Property({ unique: true })
  name!: string;

  @Property({ nullable: true })
  content?: string;

  @Property({ nullable: true })
  category?: string;

  @OneToMany(() => PerfumeNote, (perfumeNote) => perfumeNote.note)
  perfumeNotes = new Collection<PerfumeNote>(this);

  constructor(name: string, content?: string, category?: string) {
    super();
    this.name = name;
    this.content = content;
    this.category = category;
  }
}

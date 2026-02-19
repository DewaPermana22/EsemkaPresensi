using System;
using System.Collections.Generic;

namespace LKS_ITSSA_2025.Models;

public partial class Gaji
{
    public int Id { get; set; }

    public decimal? Gaji1 { get; set; }

    public int? UserId { get; set; }

    public virtual ICollection<Penggajian> Penggajians { get; set; } = new List<Penggajian>();

    public virtual User? User { get; set; }
}

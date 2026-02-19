using System;
using System.Collections.Generic;

namespace LKS_ITSSA_2025.Models;

public partial class Penggajian
{
    public int Id { get; set; }

    public int GajiId { get; set; }

    public int? UserId { get; set; }
    
    public decimal? Bonus { get; set; }

    public decimal? Pelanggaran { get; set; }

    public decimal? Total { get; set; }

    public byte? SudahSelesai { get; set; }

    public virtual Gaji Gaji { get; set; } = null!;

    public virtual User? User { get; set; }
}

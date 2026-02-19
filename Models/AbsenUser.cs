using System;
using System.Collections.Generic;
using System.Text.Json.Serialization;

namespace LKS_ITSSA_2025.Models;

public partial class AbsenUser
{
    public int Id { get; set; }

    public int? UserId { get; set; }

    public int? StatusId { get; set; }

    public DateTime? JamMasuk { get; set; }

    public DateTime? JamKeluar { get; set; }
    public DateOnly? Tanggal { get; set; } = DateOnly.FromDateTime(DateTime.Now);

    public string? SelfieMasuk { get; set; }
    public string? SelfieKeluar { get; set; }
    [JsonIgnore]
    public virtual StatusAbsen? Status { get; set; }

    [JsonIgnore]
    public virtual User? User { get; set; }
}
